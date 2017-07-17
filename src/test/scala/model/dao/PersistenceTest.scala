package model.dao

import model._
import model.persistence._

case class CrashTestDummy(a: String, id: Int)

class PersistenceTest extends TestSpec {
  "Copier" should {
    "work" in {
        val x = CrashTestDummy("hi", 123)
        val result = Copier(x, ("id", 456))
        result shouldBe CrashTestDummy("hi", 456)
    }
  }

  "Cached instances" should  {
    "pass autoinc" in {
      Users.setAutoInc()
    }

    "create via upsert" in {
      val user: User = Users.upsert(User(
        userId = s"user0",
        email = s"user0@gmail.com",
        firstName = s"Joe0",
        lastName = "Smith",
        password = "secret",
        paymentMechanism = PaymentMechanism.NONE,
        paymentMechanisms = List(PaymentMechanism.PAYPAL_REST, PaymentMechanism.SQUARE, PaymentMechanism.STRIPE)
      ))
      user.id.value shouldBe Some(1L)
    }

    "be found by id" in {
      Users.findById(Id(Option(1L))).size shouldBe 1
    }

    "be found by userId" in {
      Users.findByUserId("user0").size shouldBe 1
    }

    "delete by id" in {
      Users.deleteById(Id(Option(1L)))
      Users.findAll.size shouldBe 0
    }

    "create via insert" in {
      val user0: User = Users.insert(User(
        userId = "user0",
        email = "user0@gmail.com",
        firstName = s"Joe0",
        lastName = "Smith",
        password = "secret",
        paymentMechanism = PaymentMechanism.NONE,
        paymentMechanisms = List(PaymentMechanism.PAYPAL_REST, PaymentMechanism.SQUARE, PaymentMechanism.STRIPE)
      ))
      Users._findAll.size shouldBe 1
      Users.findAllFromDB.size shouldBe 1
      Users.findAll.size shouldBe 1
      user0.id.value shouldBe Some(2L)
    }

    "updated via upsert" in {
      val user = Users.findAll.head
      val modified = user.copy(userId = "xxx")
      modified.id.value shouldBe user.id.value
      modified.userId shouldBe "xxx"

      val upserted: User = Users.upsert(modified)
      Users._findAll.size shouldBe 1
      Users.findAllFromDB.size shouldBe 1
      Users.findAll.size shouldBe 1
      upserted.id.value shouldBe user.id.value
      upserted.userId shouldBe "xxx"
    }

    "inserted via upsert" in {
      val newUser: User = Users.upsert(User(
        userId = "user",
        email = "user@gmail.com",
        firstName = s"Mary",
        lastName = "Jane",
        password = "notTelling",
        paymentMechanism = PaymentMechanism.NONE,
        paymentMechanisms = Nil
      ))
      Users._findAll.size shouldBe 2
      Users.findAllFromDB.size shouldBe 2
      Users.findAll.size shouldBe 2
      newUser.userId shouldBe "user"
    }
  }

  "Uncached instances" should  {
    "pass autoinc" in {
      Tokens.setAutoInc()
    }

    "create via upsert" in {
      val token: Token = Tokens.upsert(Token(
        value = "asdf"
      ))
      token.id.value shouldBe Some(1L)
    }

    "be found by id" in {
      Tokens.findById(Id(Option(1L))).size shouldBe 1
    }

    "delete by id" in {
      Tokens.deleteById(Id(Option(1L)))
      Tokens.findAll.size shouldBe 0
    }

    "create via insert" in {
      val user: Token = Tokens.insert(Token(
        value = "value"
      ))
      Tokens._findAll.size shouldBe 1
      Tokens.findAllFromDB.size shouldBe 1
      Tokens.findAll.size shouldBe 1
      user.id.value shouldBe Some(2L)
    }
  }

  "Connection pool" should {
    "work" in {
      (1L to 299L).foreach { i =>
        Users.upsert(User(
          userId = s"user$i",
          email = s"user$i@gmail.com",
          firstName = s"Joe$i",
          lastName = "Smith",
          password = "secret",
          paymentMechanism = PaymentMechanism.NONE,
          paymentMechanisms = List(PaymentMechanism.PAYPAL_REST, PaymentMechanism.SQUARE, PaymentMechanism.STRIPE)
        ))
      }
      val users: Seq[User] = Users.findAll
      users.size shouldBe 301
    }
  }
}
