package model

import dao._
import persistence._
import model.Ctx.{run => qRun, _}

class FindAllTest extends TestSpec {
  "Tokens" should {
    "be created via insert" in {
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
}
