package model.persistence

import java.net.URL
import java.util.UUID
import com.github.nscala_time.time.Imports._
import io.getquill.context.jdbc.JdbcContext
import scala.reflect.ClassTag

class EnumQuillEncoder[E <: Enum[E] : ClassTag](val ctx: JdbcContext[_, _]) {
  import ctx._

  implicit val enumDecoder: Decoder[E] = decoder(java.sql.Types.VARCHAR,
    (index, row) => {
      val klass = classOf[ClassTag[E]]
      val method = klass.getDeclaredMethod("valueOf", classOf[String])
      val result = method.invoke(klass, row.getString(index))
      result.asInstanceOf[E]
    })

  implicit val enumEncoder: Encoder[E] =
    encoder(
      java.sql.Types.VARCHAR,
      (index, value, row) => row.setString(index, value.name)
    )
}

trait QuillImplicits extends IdImplicitLike with CtxLike {
  import ctx._

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


  implicit val encodeOptionIdOptionLong: MappedEncoding[String, Option[Id[Option[Long]]]] =
      MappedEncoding { x =>
        val string = x.trim
        if (string.isEmpty) None
        else Option(Id(Option(string.toLong)))
      }

  implicit val decodeOptionIdOptionLong: MappedEncoding[Option[Id[Option[Long]]], String] =
    MappedEncoding(_.mkString(","))


  implicit val encodeListIdOptionLong: MappedEncoding[String, List[Id[Option[Long]]]] =
    MappedEncoding { x =>
      val string = x.trim
      if (string.isEmpty) Nil
      else string.split(",").map { y => Id(Option(y.toLong)) }.toList
    }

  implicit val decodeListIdOptionLong: MappedEncoding[List[Id[Option[Long]]], String] =
    MappedEncoding(_.mkString(","))


  val urlEmpty = new URL("http://empty")
  implicit val encodeURL: MappedEncoding[URL, String] = MappedEncoding[URL, String] { url =>
    if (url==urlEmpty) "" else url.toString
  }
  implicit val decodeURL: MappedEncoding[String, URL] = MappedEncoding[String, URL] { x =>
    val string = x.trim
    if (string.isEmpty) urlEmpty else new URL(string)
  }

  implicit val encodeOptionURL: MappedEncoding[Option[URL], String] = MappedEncoding[Option[URL], String] { maybeUrl =>
    if (maybeUrl contains urlEmpty) "" else maybeUrl.mkString
  }
  implicit val decodeOptionURL: MappedEncoding[String, Option[URL]] =
    MappedEncoding[String, Option[URL]](x => if (x.isEmpty) None else Option(new URL(x)))

  implicit val encodeListURL: MappedEncoding[List[URL], String] = MappedEncoding[List[URL], String] { urls =>
    if (urls.isEmpty) "" else urls.filterNot(_ == urlEmpty).mkString(",")
  }
  implicit val decodeListURL: MappedEncoding[String, List[URL]] =
    MappedEncoding[String, List[URL]] { x =>
      val string = x.trim
      if (string.isEmpty) Nil else string
                                     .split(",")
                                     .map { x => new URL(x) }
                                     .filterNot(_ == urlEmpty)
                                     .toList
    }
}
