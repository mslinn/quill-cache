package model

import model.dao._
import model.persistence.Types.IdOptionLong
import model.persistence._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/** Create a context based on the `dataSource` from another Context */
case object Ctx2 extends H2Ctx(Ctx.dataSource) with QuillCacheImplicits

@RunWith(classOf[JUnitRunner])
class ContextTest extends TestSpec {
  "Tokens" should {
    "be created via insert" in {
      import model.dao.Ctx.{run => qRun, _}

      implicitly[Decoder[Map[IdOptionLong,List[Int]]]]

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

      implicitly[Decoder[Map[IdOptionLong,List[Int]]]]

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
