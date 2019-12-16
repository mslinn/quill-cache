package model

import java.net.URL
import java.time.LocalDateTime
import model.persistence.Types.IdOptionLong
import model.persistence._

case class Token(
  value: String,
  relatedId: Option[IdOptionLong] = None,
  prerequisiteIds: List[IdOptionLong] = Nil,
  homePage: Option[URL] = None,
  favoriteSites: List[URL] = Nil,
  created: LocalDateTime = LocalDateTime.now,
  override val id: Id[Option[Long]] = Id.empty
) extends HasId[Token, Option[Long]]
