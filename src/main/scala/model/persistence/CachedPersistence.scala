package model.persistence

import com.micronautics.utils.Implicits._
import language.{postfixOps, reflectiveCalls}

/** Overrides the Persistence methods which accesses the table so the cache is used instead */
abstract class CachedPersistence[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
  extends UnCachedPersistence[Key, _IdType, CaseClass]
  with CacheLike[Key, _IdType, CaseClass] {

  override def add(caseClass: CaseClass): CaseClass =
    findById(caseClass.id) match {
      case Some(_) => // entity already persisted, update and return it.
        upsert(caseClass)

      case None => // new entity, capture id and return modified entity after inserting
        val modified: CaseClass = caseClass.id.value match {
          case _: Some[Key] =>
            update(sanitize(caseClass))

          case x if x==None => // new entity; insert it and return modified entity
            insert(sanitize(caseClass))
        }
//        cacheSet(modified.id, modified)
        modified
    }

  @inline override def deleteById(id: Id[_IdType]): Unit = {
    super.deleteById(id)
    cacheRemoveId(id)
  }

  @inline override def findAll(): List[CaseClass] = theCache.getAll

  override def findById(id: Id[_IdType]): Option[CaseClass] = {
    if (Logger.isDebugEnabled) for {
      key <- id.value
      _   <- theCache.get(key)
    } Logger.trace(s"Found $id in $className cache")
    for {
      key <- id.value
      t   <- theCache.get(key).map { x =>
        Logger.trace(s"Found $id in $className cache")
        try {
          x
        } catch {
          case e: Exception =>
            println(e)
            throw e
        }
      }.orElse {
        Logger.debug(s"Attempting to fetch $className #$id from database")
        try {
          val maybeEntity: Option[CaseClass] = _findById(id)
          maybeEntity.foreach { e =>
            Logger.debug(s"Caching $className #$id")
            cacheSet(id, e)
          }
          maybeEntity
        } catch {
          case e: Exception =>
            Logger.error(e.format())
            None
        }
      }
    } yield t
  }

  @inline override def update(t: CaseClass): CaseClass = {
    cacheRemoveId(t.id)
    val copiedT: CaseClass = sanitize(t)
    cacheSet(t.id, copiedT)
    super.update(copiedT)
  }

  /** Searches by CaseClass.id, removes from cache
   * @return true if CaseClass was removed (false if the CaseClass was not defined prior) */
  @inline override def remove(caseClass: CaseClass): Unit = {
    caseClass.id.value.foreach(theCache.remove)
    super.remove(caseClass)
  }

  @inline override def upsert(t: CaseClass): CaseClass = {
    val upserted = super.upsert(t)
    upserted.id.value.foreach( key => theCache.put(key, upserted) )
    upserted
  }

  // Empty out the backing table; normally just used for testing
  @inline override def zap(): Unit = {
    super.zap()
    flushCache()
  }
}
