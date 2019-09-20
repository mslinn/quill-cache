package model.dao

import java.net.URL
import model._
import model.persistence._

case class CrashTestDummy(a: String, id: Int)

class PersistenceTest extends TestSpec {
  val MAX_INSTANCES = 299L

  "Copier" should {
    "work" in {
        val crashTestDummy = CrashTestDummy("hi", 123)
        val actual = Copier(crashTestDummy, ("id", 456))
        actual shouldBe CrashTestDummy("hi", 456)
    }
  }

  "Cached instances" should  {
    "pass autoinc" in {
      UserDAO.setAutoInc(Ctx)
    }

    "create via upsert" in {
      val user: User = UserDAO.upsert(User(
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
      UserDAO.findById(Id(Option(1L))).size shouldBe 1
    }

    "be found by userId" in {
      UserDAO.findByUserId("user0").size shouldBe 1
    }

    "delete by id" in {
      UserDAO.deleteById(Id(Option(1L)))
      UserDAO.findAll.size shouldBe 0
    }

    "create via insert" in {
      val user0: User = UserDAO.insert(User(
        userId = "user0",
        email = "user0@gmail.com",
        firstName = s"Joe0",
        lastName = "Smith",
        password = "secret",
        paymentMechanism = PaymentMechanism.NONE,
        paymentMechanisms = List(PaymentMechanism.PAYPAL_REST, PaymentMechanism.SQUARE, PaymentMechanism.STRIPE)
      ))
      UserDAO._findAll.size shouldBe 1
      UserDAO.findAllFromDB.size shouldBe 1
      UserDAO.findAll.size shouldBe 1
      user0.id.value shouldBe Some(2L)
    }

    "updated via upsert" in {
      val user = UserDAO.findAll.head
      val modified = user.copy(userId = "xxx")
      modified.id.value shouldBe user.id.value
      modified.userId shouldBe "xxx"

      val upserted: User = UserDAO.upsert(modified)
      UserDAO._findAll.size shouldBe 1
      UserDAO.findAllFromDB.size shouldBe 1
      UserDAO.findAll.size shouldBe 1
      upserted.id.value shouldBe user.id.value
      upserted.userId shouldBe "xxx"
    }

    "inserted via upsert" in {
      val newUser: User = UserDAO.upsert(User(
        userId = "user",
        email = "user@gmail.com",
        firstName = s"Mary",
        lastName = "Jane",
        password = "notTelling",
        paymentMechanism = PaymentMechanism.NONE,
        paymentMechanisms = Nil
      ))
      UserDAO._findAll.size shouldBe 2
      UserDAO.findAllFromDB.size shouldBe 2
      UserDAO.findAll.size shouldBe 2
      newUser.userId shouldBe "user"
    }
  }

  "Uncached instances" should  {
    "pass autoinc" in {
      TokenDAO.setAutoInc(Ctx)
    }

    "create via upsert" in {
      val token: Token = TokenDAO.upsert(Token(
        value = "asdf"
      ))
      token.id.value shouldBe Some(1L)
    }

    "be found by id" in {
      TokenDAO.findById(Id(Option(1L))).size shouldBe 1
    }

    "delete by id" in {
      TokenDAO.deleteById(Id(Option(1L)))
      TokenDAO.findAll.size shouldBe 0
    }

    "create via insert" in {
      val user: Token = TokenDAO.insert(Token(
        value = "value"
      ))
      TokenDAO._findAll.size shouldBe 1
      TokenDAO.findAllFromDB.size shouldBe 1
      TokenDAO.findAll.size shouldBe 1
      user.id.value shouldBe Some(2L)
    }
  }

  "List of Java Enums" should {
    "decode" in {
      val idOptionLong = Id(Option(1L))
      val token: Token = TokenDAO.upsert(Token(
        value = "asdf",
        prerequisiteIds = List(idOptionLong)
      ))
      TokenDAO.findById(token.id) foreach { token =>
        token.prerequisiteIds should contain (idOptionLong)
      }
    }
  }

  "Collections of URL" should {
    "decode" in {
      val homePage = Some(new URL("https://www.mslinn.com"))
      val scalaCourses = new URL("https://www.scalacourses.com")
      val token: Token = TokenDAO.upsert(Token(
        value = "asdf",
        homePage = homePage,
        favoriteSites = List(scalaCourses)
      ))
      TokenDAO.findById(token.id) foreach { token =>
        token.homePage shouldBe homePage
        token.favoriteSites should contain (scalaCourses)
      }
    }
  }

  "Connection pool" should {
    "work" in {
      (1L to 299L).foreach { i =>
        UserDAO.upsert(User(
          userId = s"user$i",
          email = s"user$i@gmail.com",
          firstName = s"Joe$i",
          lastName = "Smith",
          password = "secret",
          paymentMechanism = PaymentMechanism.NONE,
          paymentMechanisms = List(PaymentMechanism.PAYPAL_REST, PaymentMechanism.SQUARE, PaymentMechanism.STRIPE)
        ))
      }
      val users: Seq[User] = UserDAO.findAll
      users.size shouldBe MAX_INSTANCES + 2
      UserDAO.deleteAll()
    }
  }
}
