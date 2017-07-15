import model._
import model.dao._
import model.persistence._
import model.dao.SelectedCtx
import org.scalatest._

trait LocalH2Server {
  org.h2.tools.Server.createTcpServer().start() // start H2 server
}

class MiniTest extends WordSpec with Matchers with BeforeAndAfterAll with LocalH2Server with SelectedCtx with QuillImplicits {
  val resourcePath = "evolutions/default/1.sql" // for accessing evolution file as a resource from a jar
  val fallbackPath = s"src/test/resources/$resourcePath" // for testing this project
  val processEvolution = new ProcessEvolution(resourcePath, fallbackPath)
  import ctx._

  override def beforeAll(): Unit = {
    logger.warn(s"Creating H2 embedded database from $resourcePath or $fallbackPath")
    try { processEvolution.downs(SelectedCtx) } catch { case _: Throwable => } // just in case the last session did not clean up
    processEvolution.ups(SelectedCtx)
    logger.warn("H2 embedded database should exist now.")
  }

  override def afterAll(): Unit = {
    processEvolution.downs(SelectedCtx)
  }

  "Tokens" should {
    "be created via insert" in {
      val token0: Token = Tokens.insert(Token(
        value = "value"
      ))
      val id: Id[Option[Long]] = ctx.run { quote { query[Token].insert(lift(token0)) }.returning(_.id) }
      id.value shouldBe Some(2L)

      val w: Seq[Token] = ctx.run { quote { query[Token] } }
      w.size shouldBe 2

      Brokens._findAll.size shouldBe 2
      Tokens._findAll.size shouldBe 2
    }
  }
}
