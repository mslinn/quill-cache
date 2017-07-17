package model

import java.net.URL
import model.persistence.Types.IdOptionLong
import model.persistence._
import org.joda.time.DateTime

case class Token(
  value: String,
  relatedId: Option[IdOptionLong] = None,
  prerequisiteIds: List[IdOptionLong] = Nil,
  homePage: Option[URL] = None,
  favoriteSites: List[URL] = Nil,
  created: DateTime = DateTime.now,
  override val id: Id[Option[Long]] = Id.empty
) extends HasId[Token, Option[Long]]
