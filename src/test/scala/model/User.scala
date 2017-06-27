package model

import model.persistence._

case class User(
  userId: String,
  email: String,
  firstName: String,
  lastName: String,
  password: String,
  activated: Boolean = false,
  override val id: Id[Option[Long]] = Id.empty
) extends HasId[User, Option[Long]]
