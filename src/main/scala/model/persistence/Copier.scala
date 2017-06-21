package model.persistence

object CopierTest extends App {
  case class X(a: String, id: Int)
  val x = X("hi", 123)
  val result = Copier(x, ("id", 456))
  println(result)
}

/** From [StackOverflow](http://stackoverflow.com/questions/17312254/scala-case-class-copy-with-dynamic-named-parameter) */
object Copier {
  def apply[T](o: T, vals: (String, Any)*): T = {
    val copier = new Copier(o.getClass)
    copier(o, vals: _*)
  }
}

/** Utility class for providing copying of a designated case class with minimal overhead. */
class Copier(cls: Class[_]) {
  import java.lang.reflect.{Constructor, Method, Modifier}

  private val ctor: Constructor[_] = cls.getConstructors.apply(0)
  private val getters: Array[Method] = cls.getDeclaredFields
    .filter { f =>
      val m: Int = f.getModifiers
      Modifier.isPrivate(m) && Modifier.isFinal(m) && !Modifier.isStatic(m)
    }
    .take(ctor.getParameterTypes.length)
    .map(f => cls.getMethod(f.getName))

  /** A reflective case class copier */
  def apply[T](o: T, vals: (String, Any)*): T = {
    val byIx: Map[Int, Object] = vals.map {
      case (name, value) =>
        val ix: Int = getters.indexWhere(_.getName == name)
        if (ix < 0) throw new IllegalArgumentException(s"Unknown field $name in ${ cls.getName }")
        (ix, value.asInstanceOf[Object])
    }.toMap

    val args: IndexedSeq[AnyRef] = getters.indices.map { i =>
      byIx.getOrElse(i, getters(i).invoke(o))
    }
    ctor.newInstance(args: _*).asInstanceOf[T]
  }
}
