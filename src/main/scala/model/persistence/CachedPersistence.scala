package model.persistence

import scala.concurrent.ExecutionContext

/** Overrides the Persistence methods which accesses the table so the cache is used instead.
  * All instances of the domain model are expected to fit into the cache. */
abstract class CachedPersistence[Key <: Any, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]](override val className: String)
                                                                                                            (implicit val ec: ExecutionContext)
  extends UnCachedPersistence[Key, _IdType, CaseClass](className)
  with CacheLike[Key, _IdType, CaseClass] {

  @inline override def deleteById(id: Id[_IdType]): Unit = {
    super.deleteById(id)
    cacheRemoveId(id)
  }

  @inline override def findAll: List[CaseClass] = theCache.getAll

  override def findById(id: Id[_IdType]): Option[CaseClass] = {
    if (logger.isDebugEnabled) for {
      key <- id.value
      _   <- theCache.get(key)
    } logger.trace(s"Found $id in $className cache")
    for {
      key <- id.value
      t   <- theCache.get(key).map { x =>
        logger.trace(s"Found $id in $className cache")
        try {
          x
        } catch {
          case e: Exception =>
            println(e)
            throw e
        }
      }.orElse {
        logger.debug(s"Attempting to fetch $className #$id from database")
        try {
          val maybeEntity: Option[CaseClass] = _findById(id)
          maybeEntity.foreach { e =>
            logger.debug(s"Caching $className #$id")
            cacheSet(id, e)
          }
          maybeEntity
        } catch {
          case e: Exception =>
            logger.error(e.format())
            None
        }
      }
    } yield t
  }

  @inline override def insert(t: CaseClass): CaseClass = {
    val copiedT: CaseClass = sanitize(t)
    val inserted = super.insert(copiedT)
    cacheSet(inserted.id, inserted)
    inserted
  }

  @inline override def update(t: CaseClass): CaseClass = {
    val copiedT: CaseClass = sanitize(t)
    val updated = super.update(copiedT)
    cacheRemoveId(t.id)
    cacheSet(updated.id, updated)
    updated
  }

  /** Searches by CaseClass.id, removes from cache
   * @return true if CaseClass was removed (false if the CaseClass was not defined prior) */
  @inline override def remove(caseClass: CaseClass): Unit = {
    caseClass.id.value.foreach(x => theCache.remove(x))
    super.remove(caseClass)
  }

  @inline override def upsert(t: CaseClass): CaseClass = {
    val upserted = super.upsert(t)
    upserted.id.value.foreach(key => theCache.put(key, upserted) )
    upserted
  }

  // Empty out the backing table; normally just used for testing
  @inline override def zap(): Unit = {
    super.zap()
    flushCache()
  }
}
