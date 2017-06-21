package model.persistence

import com.micronautics.utils.Implicits._
import org.slf4j.Logger
import scala.language.{postfixOps, reflectiveCalls}

/** Accesses the table for each query */
abstract class UnCachedPersistence[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
  extends Quill with QuillImplicits {

  protected val Logger: Logger = org.slf4j.LoggerFactory.getLogger("persistence")

  def _findAll: () => List[CaseClass]
  def _deleteById: (Id[_IdType]) => Unit
  def _findById: Id[_IdType] => Option[CaseClass]
  def _insert: CaseClass => CaseClass
  def _update: CaseClass => CaseClass

  /** Human-readable name of persisted class */
  val className: String

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

  def findAll: List[CaseClass] = {
    Logger.debug(s"Fetching all ${ className }s from database")
    try { _findAll() } catch {
      case ex: Exception =>
        Logger.error(ex.format())
        Nil
    }
  }

  /** Always fetches from the database; bypass cache, useful for startup code.. See also [[CachedPersistence.findAll]] */
  @inline def findAllFromDB: List[CaseClass] = {
    try {
      val caseClasses: List[CaseClass] = _findAll()
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

  @inline def insert(caseClass: CaseClass): CaseClass = _insert(caseClass)

  /** Searches by `CaseClass.id`
   * @return true if `CaseClass` was removed (false if the `CaseClass` was not defined prior) */
  @inline def remove(t: CaseClass): Unit = t.id.value match {
    case _: Some[_IdType] => deleteById(t.id)
    case x if x==None => ()
  }

  @inline def update(caseClass: CaseClass): CaseClass = _update(caseClass)

  @inline def upsert(caseClass: CaseClass): CaseClass =
    try {
      val notFound = findById(caseClass.id).isEmpty
      if (notFound) insert(caseClass) else update(caseClass)
    } catch {
      case ex: Exception =>
        Logger.error(s"upsert $caseClass" + ex.format())
        throw ex
    }

  // Empty out the backing table; normally just used for testing
  @inline def zap(): Unit = _findAll().foreach(remove)

  protected val sanitize: CaseClass => CaseClass =
    (t: CaseClass) => t

  protected val copyUpdate: (CaseClass, Id[_IdType]) => CaseClass =
    (t: CaseClass, id: Id[_IdType]) => sanitize(t.setId(id))
}
