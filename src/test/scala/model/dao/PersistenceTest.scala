package model.dao

import model.User
import model.persistence._
import model.persistence.macros._
import org.scalatest._
import scala.language.experimental.macros

case class X(a: String, id: Int)

class PersistenceTest extends WordSpec with Matchers with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    ProcessEvolutionUp.apply(new java.io.File("src/test/resources/evolutions/default/1.sql"))
    println("Database should exist now.")
  }

  "Copier" should {
    "work" in {
        val x = X("hi", 123)
        val result = Copier(x, ("id", 456))
        result shouldBe X("hi", 456)
    }
  }

  "InsertOrUpdateMacro" should {
    "work" in {
      def upsert[T](entity: T, filter: T => Boolean): Unit = macro InsertOrUpdateMacro.insertOrUpdate[T]
      val user = User(userId = s"userX", email = s"userx@gmail.com", firstName = s"Joe", lastName = "Smith", password = "secret")
      upsert(user, (user: User) => user.id.value.isEmpty)
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
