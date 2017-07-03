package model.dao

import model.persistence.{Copier, Id, ProcessEvolutionUp}
import model.{SelectedCtx, User}
import org.scalatest._

case class X(a: String, id: Int)

class PersistenceTest extends WordSpec with Matchers with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    println("Creating H2 in-memory database from test/resources/evolutions/default/1.sql.")
    ProcessEvolutionUp(SelectedCtx, "evolutions/default/1.sql")
    println("H2 in-memory database should exist now.")
  }

  "Copier" should {
    "work" in {
        val x = X("hi", 123)
        val result = Copier(x, ("id", 456))
        result shouldBe X("hi", 456)
    }
  }

  "Course instances" should  {
    "work" in {
      val user: User = Users.upsert(User(userId = "user0", email = "user0@gmail.com", firstName = "Joe0", lastName = "Smith", password = "secret"))
      user.id.value shouldBe Some(1L)
    }

    "be found by id" in {
      Users.findById(Id(Option(1L))).size shouldBe 1
    }
  }

  "Connection pool" should {
    "work" in {
      (1L to 299L).foreach { i =>
        Users.upsert(User(userId = s"user$i", email = s"user$i@gmail.com", firstName = s"Joe$i", lastName = "Smith", password = "secret"))
      }
    }
  }
}
