package model

import model.dao.SelectedCtx
import model.persistence._
import org.h2.tools.Server
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

abstract class TestSpec extends WordSpec with Matchers with LocalH2Server with SelectedCtx with QuillImplicits with SetupTeardown

trait LocalH2Server {
  val h2Server: Server = org.h2.tools.Server.createTcpServer()
  h2Server.start()
}

// Because BeforeAndAfterAll invokes super.run, mix this trait in last
trait SetupTeardown extends BeforeAndAfterAll { this: LocalH2Server =>
  val resourcePath = "evolutions/default/1.sql" // for accessing evolution file as a resource from a jar
  val fallbackPath = s"src/test/resources/$resourcePath" // for testing this project
  val processEvolution = new ProcessEvolution(resourcePath, fallbackPath)

  override def beforeAll(): Unit = {
    try { h2Server.start() } catch { case _: Throwable => }

    logger.warn(s"Creating H2 embedded database from $resourcePath or $fallbackPath")
    try { processEvolution.downs(SelectedCtx) } catch { case _: Throwable => } // just in case the last session did not clean up
    processEvolution.ups(SelectedCtx)
    logger.warn("H2 embedded database should exist now.")
  }

  override def afterAll(): Unit = {
    processEvolution.downs(SelectedCtx)

    h2Server.stop()
  }
}
