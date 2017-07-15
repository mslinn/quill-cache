package model.dao

import model.persistence._
import model.{PaymentMechanism, Token, User}
import org.scalatest._

case class CrashTestDummy(a: String, id: Int)

class PersistenceTest extends WordSpec with Matchers with BeforeAndAfterAll with SelectedCtx {
  val resourcePath = "evolutions/default/1.sql" // for accessing evolution file as a resource from a jar
  val fallbackPath = s"src/test/resources/$resourcePath" // for testing this project
  val processEvolution = new ProcessEvolution(resourcePath, fallbackPath)
  org.h2.tools.Server.createTcpServer().start() // start H2 server

  override def beforeAll(): Unit = {
    logger.warn(s"Creating H2 embedded database from $resourcePath or $fallbackPath")
    try { processEvolution.downs(SelectedCtx) } catch { case _: Throwable => } // just in case the last session did not clean up
    processEvolution.ups(SelectedCtx)
    logger.warn("H2 embedded database should exist now.")
  }

  override def afterAll(): Unit = {
    processEvolution.downs(SelectedCtx)
  }

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
      val user: User = Users.insert(User(
        userId = s"user0",
        email = s"user0@gmail.com",
        firstName = s"Joe0",
        lastName = "Smith",
        password = "secret",
        paymentMechanism = PaymentMechanism.NONE,
        paymentMechanisms = List(PaymentMechanism.PAYPAL_REST, PaymentMechanism.SQUARE, PaymentMechanism.STRIPE)
      ))
      Users._findAll.size shouldBe 1
      Users.findAllFromDB.size shouldBe 1
      Users.findAll.size shouldBe 1
      user.id.value shouldBe Some(2L)
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
      users.size shouldBe 300
    }
  }
}
