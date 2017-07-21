package model.persistence

import scala.concurrent.ExecutionContext

trait CacheExecutionContext extends ExecutionContext

trait CacheLike[Key <: Any, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]] {
  protected val theCache: AbstractCache[Key, CaseClass]

  @inline def cacheRemoveId(id: Id[_IdType]): Unit = {
    logger.debug(s"Removing $id from $className cache")
    id.value.foreach(key => theCache.remove(key))
    ()
  }

  @inline def cacheSet(i: Id[_IdType], value: CaseClass): Unit = {
    i.value.foreach(key => theCache.put(key, value))
    logger.trace(s"Added $i to $className cache")
  }

  /** Human-readable name of persisted class */
  def className: String

  @inline def findAll: List[CaseClass]

  @inline def findById(id: Id[_IdType]): Option[CaseClass]

  @inline def flushCache(): Unit = {
    theCache.underlying.invalidateAll()
    logger.debug(s"Cleared $className cache")
  }

  /** Flushes the cache and then loads all instances of `CaseClass` into the cache from the database. */
  @inline def preload(): List[CaseClass] = theCache.synchronized {
    flushCache()
    val all = findAll
    all.foreach(x => cacheSet(x.id, x))
    all
  }
}

/** `SoftCache` contains "soft" values that might expire by timing out or might get bumped if memory fills up.
  * Mix this trait into the DAO to provide this behavior.
  * DAOs that mix in `SoftCache` do not assume that all instances of the case class can fit into memory.
  *
  * `SoftCache` finders that return at most one item from querying the cache will access the database, looking for that item after every cache miss.
  * Because of this, those `SoftCache` finders run more slowly than `StrongCache` finders when the cache does not contain the desired value.
  *
  * `SoftCache` finders that return a list of items must always query the database and never look in the cache.
  *
  * The `CachedPersistence` trait implements the default caching strategy.
  * This trait overrides the default finder implementations.
  * This trait is experimental, do not use in production. */
  // TODO implement the [[https://google.github.io/guava/releases/16.0/api/docs/com/google/common/cache/CacheBuilder.html#removalListener(com.google.common.cache.RemovalListener) com.google.common.cache.CacheBuilder.removalListener]] callback to detect when a cache has been partially flushed due to timeout or memory pressure.
trait SoftCacheLike[Key <: Any, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
    extends CacheLike[Key, _IdType, CaseClass] { cp: CachedPersistence[Key, _IdType, CaseClass] =>
  implicit val cacheExecutionContext: CacheExecutionContext = implicitly[CacheExecutionContext]
  protected val theCache: SoftCache[Key, CaseClass] = SoftCache[Key, CaseClass]()

  /** Cannot assume all values are cached, so always get them from the database */
  @inline override def findAll: List[CaseClass] = cp._findAll

  /** First try to fetch from cache, then if not found try to fetch from the database */
  @inline abstract override def findById(id: Id[_IdType]): Option[CaseClass] = super.findById(id).orElse(_findById(id))
}

/** `CachePersistence.prefetch` must be called before any finders.
  * The `CachedPersistence` trait implements the default caching strategy.
  * This trait overrides the default finder implementations. */
trait StrongCacheLike[Key <: Any, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
    extends CacheLike[Key, _IdType, CaseClass] { cp: CachedPersistence[Key, _IdType, CaseClass] =>
  implicit val cacheExecutionContext: CacheExecutionContext = implicitly[CacheExecutionContext]
  protected val theCache: StrongCache[Key, CaseClass] = StrongCache[Key, CaseClass]()

  @inline override def findAll: List[CaseClass] =
    theCache.underlying.asMap.values.toArray.toList.asInstanceOf[List[CaseClass]]

//  @inline def findAllFromDB: List[CaseClass] = cp._findAll
}
