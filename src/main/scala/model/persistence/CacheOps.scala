package model.persistence

trait CacheOps[Key <: Object, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]] {
  def cacheSet(i: Id[_IdType], value: CaseClass): Unit

  def cacheSet(key: String, value: CaseClass): Unit

  def cacheClear(): Unit

  def cacheRemoveId(id: Id[_IdType]): Unit
}
