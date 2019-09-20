package model

import model.persistence.Types.IdOptionLong
import model.persistence._

case class User(
  userId: String,
  email: String,
  firstName: String,
  lastName: String,
  paymentMechanism: PaymentMechanism,
  paymentMechanisms: List[PaymentMechanism],
  wrongAnswerMap: Map[IdOptionLong, List[Int]] = Map.empty,
  questionIds: Vector[IdOptionLong] = Vector.empty,
  correctAnswerIds: List[IdOptionLong] = Nil,
  writtenScore: Option[Int] = None,
  password: String,
  activated: Boolean = false,
  override val id: Id[Option[Long]] = Id.empty
) extends HasId[User, Option[Long]]
