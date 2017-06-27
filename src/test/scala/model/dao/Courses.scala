package model.dao

import model.Course
import model.persistence.Types._
import model.persistence._
import scala.concurrent.ExecutionContext

object Courses extends CachedPersistence[Long, OptionLong, Course] with StrongCacheLike[Long, OptionLong, Course] {
  import QuillConfiguration.ctx._

  /** A real application would provide a dedicated `ExecutionContext` for DAOs */
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override val _findAll: List[Course] =
    run { quote { query[Course] } }

  val queryById: IdOptionLong => Quoted[EntityQuery[Course]] =
    (id: Id[OptionLong]) =>
      quote { query[Course].filter(_.id == lift(id)) }

  val _deleteById: (IdOptionLong) => Unit =
    (id: Id[OptionLong]) => {
      run { quote { queryById(id).delete } }
      ()
    }

  val _findById: IdOptionLong => Option[Course] =
    (name: Id[OptionLong]) =>
      run { quote { queryById(name) } }.headOption

  val _insert: Course => Course =
    (course: Course) => {
      val id: IdOptionLong = try {
        run { quote { query[Course].insert(lift(course)) }.returning(_.id) }
      } catch {
        case e: Throwable =>
          Logger.error(e.getMessage)
          throw e
      }
      course.copy(id=id)
    }

  val _update: Course => Course =
    (course: Course) => {
      val id: Long = run { queryById(course.id).update(lift(course)) }
      course.copy(id = Id(Some(id)))
    }

  val className: String = "Course"

  override protected val sanitize: (Course) => Course =
    (course: Course) => course

  // On startup
  setAutoInc()


  /** Inserts newCourse into database and augments cache. */
  override def add(newCourse: Course): Course =
    findBySku(newCourse.sku) match {
      case Some(course) => // course already exists, update it and return it
        upsert(course)

      case None =>
        val modifiedCourse: Course = newCourse.id.value match {
          case _: Some[Long] => // todo consolidate with save?
            val sanitizedCourse: Course = sanitize(newCourse)
            update(sanitizedCourse)
            sanitizedCourse

          case None => // new course; insert it and return modified course
            val sanitizedCourse = sanitize(newCourse)
            val inserted: Course = insert(sanitizedCourse)
            cacheSet(inserted.id, inserted)
            inserted
        }
        cacheSet(modifiedCourse.id, modifiedCourse)
        modifiedCourse
    }

  /** @return Option[Course] with specified SKU */
  @inline def findBySku(sku: String): Option[Course] = findAll.find(_.sku == longSku(sku))

  /** Returns all courses in the specified group */
  @inline def findByGroupId(groupId: IdOptionLong): List[Course] = findAll.filter(_.groupId == groupId)


  @inline def longSku(sku: String): String = sku match {
    case name if name.startsWith(s"${ tableName }_") =>
      name

    case name if name.startsWith(tableName) =>
      s"${ tableName }_" + name.substring(tableName.length)

    case _ =>
      s"${ tableName }_$sku"
  }
}
