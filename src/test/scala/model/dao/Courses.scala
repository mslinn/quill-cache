package model.dao

import io.getquill.PostgresJdbcContext
import model.Course
import model.persistence._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object Courses extends CachedPersistence[Long, Option[Long], Course] with StrongCacheLike[Long, Option[Long], Course] {
  // TODO How to get rid of the `asInstanceOf` abomination?
  val _dbWitness = QuillConfiguration.dbWitness.asInstanceOf[DbWitness[PostgresJdbcContext[TableNameSnakeCase]]]
  import _dbWitness.ctx._

  /** A real application would provide a dedicated `ExecutionContext` for DAOs */
  implicit val ec: ExecutionContext = global

  override val _findAll: List[Course] =
    run { quote { query[Course] } }

  val queryById: Id[Option[Long]] => Quoted[EntityQuery[Course]] =
    (id: Id[Option[Long]]) =>
      quote { query[Course].filter(_.id == lift(id)) }

  val _deleteById: (Id[Option[Long]]) => Unit =
    (id: Id[Option[Long]]) => {
      run { quote { queryById(id).delete } }
      ()
    }

  val _findById: Id[Option[Long]] => Option[Course] =
    (name: Id[Option[Long]]) =>
      run { quote { queryById(name) } }.headOption

  val _insert: Course => Course =
    (course: Course) => {
      val id: Id[Option[Long]] = try {
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
  val tableName: String = className.toLowerCase
  val skuBase: String = className.toLowerCase

  override protected val sanitize: (Course) => Course =
    (course: Course) => course

  // On startup:
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

          case None => // new group; insert it and return modified course
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
  @inline def findByGroupId(groupId: Id[Option[Long]]): List[Course] = findAll.filter(_.groupId == groupId)


  @inline def longSku(sku: String): String = sku match {
    case name if name.startsWith(s"${ skuBase }_") =>
      name

    case name if name.startsWith(skuBase) =>
      s"${ skuBase }_" + name.substring(skuBase.length)

    case _ =>
      s"${ skuBase }_$sku"
  }

  /** Ensure that autoInc value is properly set when the app starts
    * Assumes that column named `id` is present, and that `${ tableName }_id_seq` exists.
    * @see [[https://stackoverflow.com/a/244265/553865]]
    * List sequences with {{{\ds}}} */
  @inline def setAutoInc(): Unit = {
    val maxId: Long = executeQuerySingle(s"SELECT Max(id) FROM $tableName", extractor = rs => rs.getLong(1))
    executeAction(s"ALTER SEQUENCE ${ tableName }_id_seq RESTART WITH ${ maxId + 1L }")
    ()
  }
}
