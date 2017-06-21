package model.persistence

trait CacheOps[T] {
  def cacheSet(i: Id[Option[Long]], value: T): Unit

  def cacheSet(key: String, value: T): Unit

  def cacheClear(): Unit

  def cacheRemoveId(id: Id[Option[Long]]): Unit
}
