package model

import model.persistence.Types.IdOptionLong
import model.persistence._
import org.joda.time.DateTime

case class Token(
  value: String,
  relatedId: Option[IdOptionLong] = None,
  prerequisiteIds: List[IdOptionLong] = Nil,
  created: DateTime = DateTime.now,
  override val id: Id[Option[Long]] = Id.empty
) extends HasId[Token, Option[Long]]
