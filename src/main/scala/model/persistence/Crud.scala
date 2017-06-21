package model.persistence

import java.lang

trait Crud[Key <: lang.Long, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]] {
  def upsert(t: CaseClass): CaseClass

  def findById(id: Id[Option[Long]]): Option[CaseClass]

  def remove(t: CaseClass): Unit

  def findAll: List[CaseClass]
}
