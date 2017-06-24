package model.persistence

import com.github.nscala_time.time.Imports._
import java.util.UUID
import io.getquill._

class QuillImplicits extends IdImplicitLike {
  // TODO what is the type of dbWitness.ctx?
  val dbWitness = QuillConfiguration.dbWitness
  import dbWitness.ctx._

  implicit val dateTimeDecoder: Decoder[DateTime] =
    decoder(java.sql.Types.TIMESTAMP, (index, row) => new DateTime(row.getTimestamp(index).getTime))

  implicit val dateTimeEncoder: Encoder[DateTime] =
    encoder(
      java.sql.Types.TIMESTAMP,
      (index, value, row) => row.setTimestamp(index, new java.sql.Timestamp(value.getMillis))
    )

  implicit val idLongDecoder: Decoder[Id[Long]] =
    decoder(java.sql.Types.BIGINT, (index, row) => Id(row.getLong(index)))

  implicit val idLongEncoder: Encoder[Id[Long]] =
    encoder(java.sql.Types.BIGINT, (index, value, row) => row.setLong(index, value.value))


  implicit val idOptionLongDecoder: Decoder[Id[Option[Long]]] =
    decoder(java.sql.Types.BIGINT, (index, row) => Id(Some(row.getLong(index))))

  implicit val idOptionLongEncoder: Encoder[Id[Option[Long]]] =
    encoder(java.sql.Types.BIGINT, (index, value, row) =>
      value.value match {
        case Some(v) => row.setLong(index, v)
        case None    => row.setNull(index, java.sql.Types.BIGINT)
      })

  /** @see [[https://github.com/getquill/quill/issues/805#issuecomment-309304298]] */
  import io.getquill.MappedEncoding

  implicit val encodeIdUUID: MappedEncoding[UUID, Id[UUID]] = MappedEncoding(Id.apply(_))
  implicit val decodeIdUUID: MappedEncoding[Id[UUID], UUID] = MappedEncoding(_.value)

  implicit val encodeIdOptionUUID: MappedEncoding[UUID, Id[Option[UUID]]] = MappedEncoding(x => Id(Some(x)))
  implicit val decodeIdOptionUUID: MappedEncoding[Id[Option[UUID]], UUID] =
    MappedEncoding(_.value.getOrElse(Id.empty[UUID].value))

  implicit val encodeIdLong: MappedEncoding[Long, Id[Long]] = MappedEncoding(Id.apply(_))
  implicit val decodeIdLong: MappedEncoding[Id[Long], Long] = MappedEncoding(_.value)

  implicit val encodeIdOptionLong: MappedEncoding[Long, Id[Option[Long]]] = MappedEncoding(x => Id(Some(x)))
  implicit val decodeIdOptionLong: MappedEncoding[Id[Option[Long]], Long] =
    MappedEncoding(_.value.getOrElse(Id.empty[Long].value))
}

object QuillImplicits extends QuillImplicits

/** Ensures that table names are quoted and snake_case but never start with a leading _. */
trait TableNameSnakeCase extends NamingStrategy with Escape with SnakeCase {
  override def table(s: String): String   = {
    val x = super.default(s)
    val y = if (x.startsWith("_")) x.substring(1) else x
    s""""$y""""
  }
}

object TableNameSnakeCase extends TableNameSnakeCase
