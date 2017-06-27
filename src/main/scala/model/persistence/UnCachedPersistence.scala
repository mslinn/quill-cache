package model.persistence

import io.getquill.PostgresJdbcContext
import org.slf4j.Logger
import scala.language.{postfixOps, reflectiveCalls}

/** Accesses the table for each query.
  * You can use this abstract class to derive DAOs for case classes that must have direct access to the database so the
  * case classes are not cached. You don't have to subclass `UnCachedPersistence`, but if you do then the DAOs for your
  * cached domain objects will have the same interface as the DAOs for your uncached domain objects. */
abstract class UnCachedPersistence[Key <: Any, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
  extends QuillImplicits with IdImplicitLike {
  import QuillConfiguration.ctx
  import ctx._

  protected val Logger: Logger = org.slf4j.LoggerFactory.getLogger("persistence")

  /** Encapsulates the Quill query that returns all instances of the case class from the database */
  def _findAll: List[CaseClass]

  /** Encapsulates the Quill query that deletes the instance of the case class with the given `Id` from the database */
  def _deleteById: (Id[_IdType]) => Unit

  /** Encapsulates the Quill query that optionally returns the instance of the case class from the database with the given
    * `Id`, or `None` if not found. */
  def _findById: Id[_IdType] => Option[CaseClass]

  /** Encapsulates the Quill query that inserts the given instance of the case class into the database, and returns the
    * case class as it was stored, including any auto-increment fields. */
  def _insert: CaseClass => CaseClass

  /** Encapsulates the Quill query that updates the given instance of the case class into the database, and returns the entity.
    * Throws an Exception if the case class was not previously persisted. */
  def _update: CaseClass => CaseClass

  /** Human-readable name of persisted class */
  def className: String

  lazy val tableName: String = className.toLowerCase

  @inline def add(caseClass: CaseClass): CaseClass =
    findById(caseClass.id) match {
      case Some(_) => // entity already persisted, update and return it.
        upsert(caseClass)

      case None => // new entity; return modified entity after inserting
        val modified: CaseClass = caseClass.id.value match {
          case _: Some[_IdType] =>
            val sanitized = sanitize(caseClass)
            val updated = update(sanitized)
            updated

          case x if x==None => // new entity; insert it and return modified entity
            val t2: CaseClass = insert(sanitize(caseClass))
            t2
        }
        modified
    }

  @inline def delete(caseClass: CaseClass): Unit = {
    Logger.debug(s"Deleting $className #${ caseClass.id } from database and cache")
    deleteById(caseClass.id)
    ()
  }

  @inline def deleteById(id: Id[_IdType]): Unit = {
    Logger.debug(s"Deleting $className #$id from database and cache")
    _deleteById(id)
    ()
  }

  @inline def findAll: List[CaseClass] = {
    Logger.debug(s"Fetching all ${ className }s from database")
    try { _findAll } catch {
      case ex: Exception =>
        Logger.error(ex.format())
        Nil
    }
  }

  /** Always fetches from the database; bypass cache, useful for startup code. See also [[CachedPersistence.findAll]] */
  @inline def findAllFromDB: List[CaseClass] = {
    try {
      val caseClasses: List[CaseClass] = _findAll
      Logger.trace(s"Fetched all ${ caseClasses.size } ${ className }s from database")
      caseClasses
    } catch {
      case ex: Exception =>
        Logger.error(s"_findAll $className " + ex.format())
        Nil
    }
  }

  /** Always fetches from the database; bypass cache, useful for startup code. See also `findById` */
  @inline def findByIdFromDB(id: Id[_IdType]): Option[CaseClass] = _findById(id)

  @inline def findById(id: Id[_IdType]): Option[CaseClass] = _findById(id)

  @inline def getId(t: CaseClass): Id[Key] = t.getClass.getDeclaredMethods
    .find(_.getName=="id")
    .get
    .invoke(t)
    .asInstanceOf[Id[Key]]

  @inline def insert(caseClass: CaseClass): CaseClass = _insert(caseClass)

  /** Searches by `CaseClass.id`
   * @return true if `CaseClass` was removed (false if the `CaseClass` was not defined prior) */
  @inline def remove(t: CaseClass): Unit = t.id.value match {
    case _: Some[_IdType] => deleteById(t.id)
    case x if x==None => ()
  }

  /** This method only has relevance for Postgres databases; it ensures that the next `autoInc` value is properly set when the app starts.
    * Assumes that column named `id` is present, and that `$${ tableName }_id_seq` exists.
    * There is no harm in invoking this method of other types of databases,
    * because it does not do anything unless the database is Postgres.
    * @see [[https://stackoverflow.com/a/244265/553865]]
    * List sequences with {{{\ds}}} */
  @inline def setAutoInc(): Unit = if (ctx.isInstanceOf[PostgresJdbcContext[_]]) try {
    val maxId: Long = executeQuerySingle(s"SELECT Max(id) FROM $tableName", extractor = _.getLong(1))
    executeAction(s"ALTER SEQUENCE ${ tableName }_id_seq RESTART WITH ${ maxId + 1L }")
    ()
  } catch {
    case ex: Exception =>
      Logger.error(ex.format())
  }

  @inline def update(caseClass: CaseClass): CaseClass = _update(caseClass)

  @inline def upsert(caseClass: CaseClass): CaseClass =
    try {
      val notFound = caseClass.id.value.isEmpty || findById(caseClass.id).isEmpty
      if (notFound) insert(caseClass) else update(caseClass)
    } catch {
      case ex: Exception =>
        Logger.error(s"upsert $caseClass" + ex.format())
        throw ex
    }

  // Empty out the backing table; normally just used for testing
  @inline def zap(): Unit = _findAll.foreach(remove)

  protected val sanitize: CaseClass => CaseClass =
    (t: CaseClass) => t

  protected val copyUpdate: (CaseClass, Id[_IdType]) => CaseClass =
    (t: CaseClass, id: Id[_IdType]) => sanitize(t.setId(id))
}
