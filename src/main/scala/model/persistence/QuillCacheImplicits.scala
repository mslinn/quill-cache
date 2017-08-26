package model.persistence

import java.net.URL
import java.sql.Timestamp
import java.util.UUID
import io.getquill._
import io.getquill.context.jdbc.JdbcContext
import model.persistence.Types.{IdLong, IdOptionLong}
import org.joda.time.DateTime
import scala.language.implicitConversions

trait QuillCacheImplicits extends IdImplicitLike { ctx: JdbcContext[_, _] =>
  import ctx._

  implicit val dateTimeEncoder: MappedEncoding[DateTime, Timestamp] =
    MappedEncoding[DateTime, Timestamp](x => new Timestamp(x.getMillis))
  implicit val dateTimeDecoder: MappedEncoding[Timestamp, DateTime] =
    MappedEncoding[Timestamp, DateTime](new DateTime(_))

  implicit val idLongEncoder: MappedEncoding[IdLong, Long] =
    MappedEncoding[IdLong, Long](_.value)
  implicit val idLongDecoder: MappedEncoding[Long, IdLong] =
    MappedEncoding[Long, IdLong](new Id(_))

  implicit val idOptionLongEncoder: MappedEncoding[IdOptionLong, Long] =
    MappedEncoding[IdOptionLong, Long](_.value.getOrElse(Id.empty[Long].value))
  implicit val idOptionLongDecoder: MappedEncoding[Long, IdOptionLong] =
    MappedEncoding[Long, IdOptionLong](x => new Id(Option(x)))


  @inline implicit def toByteArray(string: String): Array[Byte] =
    string
      .toCharArray
      .map(_.toByte)

  @inline implicit def toString(byteArray: Array[Byte]): String = new String(byteArray)


  /** Stores map to "key1->3,4,5;key2->1,2,3" */
  implicit val mapIdOptionLongListIntEncoder: MappedEncoding[Map[IdOptionLong, List[Int]], String] =
    MappedEncoding[Map[IdOptionLong, List[Int]], String] { inputMap =>
        inputMap.map { case (key, value) =>
          s"$key->${ value.mkString(",") }"
        }.mkString(";")
      }

  /** Retrieves map as "key1->3,4,5;key2->1,2,3" */
  implicit val mapIdOptionLongListIntDecoder: MappedEncoding[String, Map[IdOptionLong, List[Int]]] =
    MappedEncoding[String, Map[IdOptionLong, List[Int]]] { inputString =>
      val arrayOfTuples: Array[(Id[Option[Long]], List[Index])] = for {
        token <- inputString.split(";")
        Array(key, values) = token.split("->")
      } yield Id(Option(key.toLong)) -> values.split(",").map(_.toInt).toList
      arrayOfTuples.toMap
    }


  /** @see [[https://github.com/getquill/quill/issues/805#issuecomment-309304298]] */

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
    MappedEncoding { _.mkString(",") }


  implicit val encodeVectorIdOptionLong: MappedEncoding[String, Vector[Id[Option[Long]]]] =
    MappedEncoding { x =>
      val string = x.trim
      if (string.isEmpty) Vector.empty
      else string.split(",").map { y => Id(Option(y.toLong)) }.toVector
    }

  implicit val decodeVectorIdOptionLong: MappedEncoding[Vector[Id[Option[Long]]], String] =
    MappedEncoding { _.mkString(",") }


  /** Retrieves map from "key1->3,4,5;key2->1,2,3" */
  implicit val encodeMapIdOptionLongListInt: MappedEncoding[String, Map[IdOptionLong, List[Int]]] =
    MappedEncoding { x =>
      val string = x.trim
      if (string.isEmpty) Map.empty
      else {
        val x: Array[(Id[Option[Long]], List[Index])] = for {
          token              <- string.split(";")
          Array(key, values) =  token.split("->")
        } yield Id(Option(key.toLong)) -> values.split(",").map(_.toInt).toList
        val result: Map[Id[Option[Long]], List[Index]] = x.toMap
        result
      }
    }

  /** Stores map s "key1->3,4,5;key2->1,2,3" */
  implicit val decodeMapIdOptionLongListInt: MappedEncoding[Map[IdOptionLong, List[Int]], String] =
    MappedEncoding(_
      .iterator
      .map { case (key, values) => s"$key->${ values.mkString(",") }" }
      .mkString(";"))


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
