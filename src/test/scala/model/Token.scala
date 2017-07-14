package model

import model.persistence._
import org.joda.time.DateTime

case class Token(
  value: String,
  prerequisiteIds: List[Id[Option[Long]]] = Nil,
  created: DateTime = DateTime.now,
  override val id: Id[Option[Long]] = Id.empty
) extends HasId[Token, Option[Long]]