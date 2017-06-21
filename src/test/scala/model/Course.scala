package model

import model.persistence.{HasId, Id}

case class Course(
  groupId: Id[Option[Long]],
  sku: String,
  shortDescription: String = "",
  title: String = "",
  price: Option[String] = Some("10.00"),
  video: Option[String] = None,
  projectHome: Option[String] = None,
  repository: Option[String] = None,
  image: Option[String] = None,
  sections: Option[String] = None,
  active: Boolean = false,
  paypalButtonId: Option[String] = None,
  override val id: Id[Option[Long]] = Id.empty
) extends HasId[Course, Option[Long]]
