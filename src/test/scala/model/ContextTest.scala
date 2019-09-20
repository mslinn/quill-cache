package model

import java.net.URL
import java.util.UUID
import model.dao._
import model.persistence.Types._
import model.persistence._
import org.joda.time.DateTime

/** Create a context based on the `dataSource` from another Context */
case object Ctx2 extends H2Ctx(Ctx.dataSource) with QuillCacheImplicits

class ContextTest extends TestSpec {
  "TokenDAO" should {
    "be created via insert" in {
      import model.dao.Ctx.{run => qRun, _}

      val token0: Token = TokenDAO.insert(Token(
        value = "value"
      ))
      val id: Id[Option[Long]] = qRun { quote { query[Token].insert(lift(token0)) }.returningGenerated(_.id) }
      id.value shouldBe Some(2L)

      val w: Seq[Token] = qRun { quote { query[Token] } }
      w.size shouldBe 2

      BrokenDAO._findAll.size shouldBe 2
      TokenDAO._findAll.size shouldBe 2
    }
  }

  "Decoders and encoders" should {
    "work" in {
      import model.dao.Ctx._

      {
        implicitly[Decoder[java.util.Date]]
        implicitly[Encoder[java.util.Date]]
      }
      {
        implicitly[Decoder[DateTime]]
        implicitly[Encoder[DateTime]]
      }
      {
        implicitly[Decoder[Map[IdOptionLong, List[Int]]]]
        implicitly[Encoder[Map[IdOptionLong, List[Int]]]]
      }
      {
        implicitly[Decoder[Map[IdOptionLong, List[Long]]]]
        implicitly[Encoder[Map[IdOptionLong, List[Long]]]]
      }
      {
        implicitly[Decoder[IdLong]]
        implicitly[Encoder[IdLong]]
      }
      {
        implicitly[Decoder[IdOptionLong]]
        implicitly[Encoder[IdOptionLong]]
      }
      {
        implicitly[Decoder[Map[IdOptionLong, List[Int]]]]
        implicitly[Encoder[Map[IdOptionLong, List[Int]]]]
      }
      {
        implicitly[Decoder[Id[UUID]]]
        implicitly[Encoder[Id[UUID]]]
      }
      {
        implicitly[Decoder[Id[Option[UUID]]]]
        implicitly[Encoder[Id[Option[UUID]]]]
      }
      {
        implicitly[Decoder[Option[IdOptionLong]]]
        implicitly[Encoder[Option[IdOptionLong]]]
      }
      {
        implicitly[Decoder[Map[IdOptionLong, List[Int]]]]
        implicitly[Encoder[Map[IdOptionLong, List[Int]]]]
      }
      {
        implicitly[Decoder[URL]]
        implicitly[Encoder[URL]]
      }
      {
        implicitly[Decoder[Option[URL]]]
        implicitly[Encoder[Option[URL]]]
      }
      {
        implicitly[Decoder[List[URL]]]
        implicitly[Encoder[List[URL]]]
      }
    }
  }

  "Another context" should {
    "work" in {
      import model.Ctx2.{run => qRun, _}

      val token0: Token = TokenDAO.insert(Token(
        value = "value"
      ))
      val id: Id[Option[Long]] = qRun { quote { query[Token].insert(lift(token0)) }.returningGenerated(_.id) }
      id.value shouldBe Some(4L)

      val w: Seq[Token] = qRun { quote { query[Token] } }
      w.size shouldBe 4

      BrokenDAO._findAll.size shouldBe 4
      TokenDAO._findAll.size shouldBe 4
    }
  }
}
