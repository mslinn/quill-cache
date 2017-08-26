package model.persistence

import java.net.URL
import java.util.{Date, UUID}
import io.getquill._
import io.getquill.context.jdbc.JdbcContext
import model.persistence.Types.{IdLong, IdOptionLong, OptionLong}
import org.joda.time.DateTime
import scala.language.implicitConversions

trait QuillCacheImplicits extends IdImplicitLike { ctx: JdbcContext[_, _] =>
  import ctx._

  type OptionLongToListInt    = Map[OptionLong,   List[Int]]
  type IdOptionLongToListInt  = Map[IdOptionLong, List[Int]]
  type OptionLongToListLong   = Map[OptionLong,   List[Long]]
  type IdOptionLongToListLong = Map[IdOptionLong, List[Long]]

  implicit val encodeDate: MappedEncoding[Date, DateTime] = MappedEncoding[java.util.Date, DateTime](new DateTime(_))
  implicit val decodeDate: MappedEncoding[DateTime, Date] = MappedEncoding[DateTime, java.util.Date](_.toDate)

  implicit val idOptionLongEncoder: MappedEncoding[IdOptionLong, Long] =
    MappedEncoding[IdOptionLong, Long](_.value.getOrElse(Id.empty[Long].value))
  implicit val idOptionLongDecoder: MappedEncoding[Long, IdOptionLong] =
    MappedEncoding[Long, IdOptionLong](x => new Id(Option(x)))


  implicit val encodeIdOptionLongToListIntString: MappedEncoding[IdOptionLongToListInt, String] =
    MappedEncoding[IdOptionLongToListInt, String] {
      _.map { case (key, values) => s"$key->${ values.mkString(",") }" }
       .mkString(";")
    }
  implicit val decodeIdOptionLongToListIntString: MappedEncoding[String, IdOptionLongToListInt] =
    MappedEncoding[String, IdOptionLongToListInt] { map =>
      if (map.isEmpty) Map.empty else {
        val arrayOfTuples: Array[(Id[Option[Long]], List[Int])] = for {
          token <- map.split(";")
          Array(key, values) = token.split("->")
        } yield Id(Option(key.toLong)) -> values.split(",").map(_.toInt).toList
        arrayOfTuples.toMap
      }
    }

  implicit val encodeIdOptionLongToListLongString: MappedEncoding[IdOptionLongToListLong, String] =
    MappedEncoding[IdOptionLongToListLong, String] {
      _.map { case (key, values) => s"$key->${ values.mkString(",") }" }
       .mkString(";")
    }
  implicit val decodeIdOptionLongToListLongString: MappedEncoding[String, IdOptionLongToListLong] =
    MappedEncoding[String, IdOptionLongToListLong] { map =>
      if (map.isEmpty) Map.empty else {
        val arrayOfTuples: Array[(Id[Option[Long]], List[Long])] = for {
          token <- map.split(";")
          Array(key, values) = token.split("->")
        } yield Id(Option(key.toLong)) -> values.split(",").map(_.toLong).toList
        arrayOfTuples.toMap
      }
    }

  /** @see [[https://github.com/getquill/quill/issues/805#issuecomment-309304298]] */

  implicit val encodeIdUUID: MappedEncoding[UUID, Id[UUID]] = MappedEncoding(Id.apply(_))
  implicit val decodeIdUUID: MappedEncoding[Id[UUID], UUID] = MappedEncoding(_.value)

  implicit val encodeIdOptionUUID: MappedEncoding[UUID, Id[Option[UUID]]] = MappedEncoding(x => Id(Some(x)))
  implicit val decodeIdOptionUUID: MappedEncoding[Id[Option[UUID]], UUID] =
    MappedEncoding(_.value.getOrElse(Id.empty[UUID].value))

  implicit val encodeIdLong: MappedEncoding[Long, IdLong] = MappedEncoding(Id.apply(_))
  implicit val decodeIdLong: MappedEncoding[IdLong, Long] = MappedEncoding(_.value)


  implicit val encodeOptionIdOptionLong: MappedEncoding[String, Option[IdOptionLong]] =
      MappedEncoding { x =>
        val string = x.trim
        if (string.isEmpty) None
        else Option(Id(Option(string.toLong)))
      }

  implicit val decodeOptionIdOptionLong: MappedEncoding[Option[IdOptionLong], String] =
    MappedEncoding(_.mkString(","))


  implicit val encodeListIdOptionLong: MappedEncoding[String, List[Id[Option[Long]]]] =
    MappedEncoding { x =>
      val string = x.trim
      if (string.isEmpty) Nil
      else string.split(",").map { y => Id(Option(y.toLong)) }.toList
    }

  implicit val decodeListIdOptionLong: MappedEncoding[List[Id[Option[Long]]], String] =
    MappedEncoding { _.mkString(",") }


  implicit val encodeVectorIdOptionLong: MappedEncoding[String, Vector[Id[Option[Long]]]] =
    MappedEncoding { x =>
      val string = x.trim
      if (string.isEmpty) Vector.empty
      else string.split(",").map { y => Id(Option(y.toLong)) }.toVector
    }

  implicit val decodeVectorIdOptionLong: MappedEncoding[Vector[Id[Option[Long]]], String] =
    MappedEncoding { _.mkString(",") }

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
