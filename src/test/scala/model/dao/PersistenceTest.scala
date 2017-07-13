package model.dao

import model.persistence.ProcessEvolutionUp.Logger
import model.User
import model.persistence.{Copier, Id, ProcessEvolutionUp}
import org.scalatest._

case class X(a: String, id: Int)

class PersistenceTest extends WordSpec with Matchers with BeforeAndAfterAll with SelectedCtx {
  override def beforeAll(): Unit = {
    val resourcePath = "evolutions/default/1.sql" // for accessing evolution file as a resource from a jar
    val fallbackPath = s"src/test/resources/$resourcePath" // for testing this project
    Logger.warn(s"Creating H2 in-memory database from $resourcePath or $fallbackPath")
    ProcessEvolutionUp(SelectedCtx, resourcePath, fallbackPath)
    Logger.warn("H2 in-memory database should exist now.")
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
      val user: User = Users.upsert(User(userId = s"user0", email = s"user0@gmail.com", firstName = s"Joe0", lastName = "Smith", password = "secret"))
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
