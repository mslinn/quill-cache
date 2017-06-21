package model.persistence

import com.micronautics.cache.{AbstractCache, SoftCache, StrongCache}
import org.slf4j.Logger
//import scala.concurrent.ExecutionContext.Implicits.global // TODO inject this

trait CacheLike[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]] {
  protected val theCache: AbstractCache[Key, CaseClass]
  protected val Logger: Logger
  protected implicit val ec: scala.concurrent.ExecutionContext

  @inline def cacheRemoveId(id: Id[_IdType]): Unit = {
    Logger.debug(s"Removing $id from $className cache")
    id.value.foreach(key => theCache.remove(key))
    ()
  }

  @inline def cacheSet(i: Id[_IdType], value: CaseClass): Unit = {
    i.value.foreach(key => theCache.put(key, value))
    Logger.trace(s"Added $i to $className cache")
  }

  /** Human-readable name of persisted class */
  def className: String

  @inline def findAll(): List[CaseClass]

  @inline def findById(id: Id[_IdType]): Option[CaseClass]

  @inline def flushCache(): Unit = {
    theCache.underlying.invalidateAll()
    Logger.debug(s"Cleared $className cache")
  }

  /** Loads all instances of `CaseClass` into the cache. */
  @inline def preload: List[CaseClass] = {
    flushCache()
    val all = findAll()
    all.foreach(x => cacheSet(x.id, x))
    all
  }
}

/** This trait is experimental, do not use in production.
  * The `CachedPersistence` trait implements the default caching strategy.
  * This trait overrides the default finder implementations. */
// TODO implement callback to detect when a cache has been partially flushed due to timeout or memory pressure.
trait SoftCacheLike[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
  extends CacheLike[Key, _IdType, CaseClass] { this: CachedPersistence[Key, _IdType, CaseClass] =>

  protected val theCache: SoftCache[Key, CaseClass] = SoftCache[Key, CaseClass]()

  /** Cannot assume all values are cached, so get them from DB */
  @inline override def findAll(): List[CaseClass] = _findAll()

  /** First try to fetch from cache, then if not found try to fetch from the database */
  @inline abstract override def findById(id: Id[_IdType]): Option[CaseClass] = super.findById(id).orElse(_findById(id))
}

/** Assumes all values have been prefetched */
trait StrongCacheLike[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
    extends CacheLike[Key, _IdType, CaseClass] {

  protected val theCache: StrongCache[Key, CaseClass] = StrongCache[Key, CaseClass]()

  @inline override def findAll(): List[CaseClass] =
    theCache.underlying.asMap.values.toArray.toList.asInstanceOf[List[CaseClass]]
}
