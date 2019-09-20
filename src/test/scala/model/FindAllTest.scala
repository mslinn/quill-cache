package model

import model.dao._
import model.persistence._

class FindAllTest extends TestSpec {
  "TokenDAO" should {
    import model.dao.Ctx.{run => qRun, _}

    "be created via insert" in {
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
}
