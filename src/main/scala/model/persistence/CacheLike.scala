package model.persistence

import com.micronautics.cache.{AbstractCache, SoftCache, StrongCache}
import scala.concurrent.ExecutionContext.Implicits.global // TODO inject this

trait CacheLike[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]] {
  protected val theCache: AbstractCache[Key, CaseClass]

  @inline def findAll: List[CaseClass]

  @inline def flushCache(): Unit = theCache.underlying.invalidateAll()
}

trait SoftCacheLike[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
  extends CacheLike[Key, _IdType, CaseClass] { this: CachedPersistence[Key, _IdType, CaseClass] =>

  protected val theCache: SoftCache[Key, CaseClass] = SoftCache[Key, CaseClass]()

  /** Cannot assume all values are cached, so get them from DB */
  @inline override def findAll: List[CaseClass] = _findAll()
}

/** Assumes all values have been prefetched */
trait StrongCacheLike[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
    extends CacheLike[Key, _IdType, CaseClass] {

  protected val theCache: StrongCache[Key, CaseClass] = StrongCache[Key, CaseClass]()

  @inline override def findAll: List[CaseClass] =
    theCache.underlying.asMap.values.toArray.toList.asInstanceOf[List[CaseClass]]
}
