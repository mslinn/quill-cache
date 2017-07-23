package model

import model.dao._
import model.persistence._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/** Create a context based on the `dataSource` from another Context */
case object Ctx2 extends H2Ctx(Ctx.dataSource) with QuillCacheImplicits

@RunWith(classOf[JUnitRunner])
class ContextTest extends TestSpec {
  implicitly[Ctx.JdbcDecoder[Seq[Array[Byte]]]] // should this work?
  /* Error:(13, 13) could not find implicit value for parameter e: model.dao.Ctx.JdbcDecoder[Seq[Array[Byte]]]
    implicitly[Ctx.JdbcDecoder[Seq[Array[Byte]]]]
  Error:(13, 13) not enough arguments for method implicitly: (implicit e: model.dao.Ctx.JdbcDecoder[Seq[Array[Byte]]])model.dao.Ctx.JdbcDecoder[Seq[Array[Byte]]].
  Unspecified value parameter e.
    implicitly[Ctx.JdbcDecoder[Seq[Array[Byte]]]] */

  implicitly[Ctx2.JdbcDecoder[Seq[Array[Byte]]]] // should this work?
  /* Error:(20, 13) could not find implicit value for parameter e: model.Ctx2.JdbcDecoder[Seq[Array[Byte]]]
    implicitly[Ctx2.JdbcDecoder[Seq[Array[Byte]]]] // should this work?
  Error:(20, 13) not enough arguments for method implicitly: (implicit e: model.Ctx2.JdbcDecoder[Seq[Array[Byte]]])model.Ctx2.JdbcDecoder[Seq[Array[Byte]]].
  Unspecified value parameter e.
    implicitly[Ctx2.JdbcDecoder[Seq[Array[Byte]]]] // should this work? */

  "Tokens" should {
    "be created via insert" in {
      import model.dao.Ctx.{run => qRun, _}
      val token0: Token = Tokens.insert(Token(
        value = "value"
      ))
      val id: Id[Option[Long]] = qRun { quote { query[Token].insert(lift(token0)) }.returning(_.id) }
      id.value shouldBe Some(2L)

      val w: Seq[Token] = qRun { quote { query[Token] } }
      w.size shouldBe 2

      Brokens._findAll.size shouldBe 2
      Tokens._findAll.size shouldBe 2
    }
  }

  "Another context" should {
    "work" in {
      import Ctx2.{run => qRun, _}
      val token0: Token = Tokens.insert(Token(
        value = "value"
      ))
      val id: Id[Option[Long]] = qRun { quote { query[Token].insert(lift(token0)) }.returning(_.id) }
      id.value shouldBe Some(4L)

      val w: Seq[Token] = qRun { quote { query[Token] } }
      w.size shouldBe 4

      Brokens._findAll.size shouldBe 4
      Tokens._findAll.size shouldBe 4
    }
  }
}
