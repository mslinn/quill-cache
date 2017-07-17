package model

import H2ServerStatus._
import LocalH2Server.h2ServerStatus
import model.dao.SelectedCtx
import model.persistence._
import org.h2.tools.Server
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}


abstract class TestSpec
  extends WordSpec
    with Matchers
    with LocalH2Server
    with SelectedCtx
    with QuillImplicits
    with SetupTeardown

object LocalH2Server {
  var h2ServerStatus: H2ServerStatus = DOES_NOT_EXIST
}

trait LocalH2Server {
  import LocalH2Server._

  val h2Server: Server = if (h2ServerStatus == DOES_NOT_EXIST) {
    logger.warn("Creating H2 TCP server")
    val h2: Server = org.h2.tools.Server.createTcpServer()
    h2ServerStatus = CREATED
    h2
  } else h2Server

  if (h2ServerStatus == CREATED) {
    logger.warn("Starting H2 TCP server")
    h2Server.start()
    h2ServerStatus = RUNNING
  }
}

// Because BeforeAndAfterAll invokes super.run, mix this trait in last
trait SetupTeardown extends BeforeAndAfterAll { this: WordSpec with LocalH2Server =>
  val resourcePath = "evolutions/default/1.sql" // for accessing evolution file as a resource from a jar
  val fallbackPath = s"src/test/resources/$resourcePath" // for testing this project
  val processEvolution = new ProcessEvolution(resourcePath, fallbackPath)

  override def beforeAll(): Unit = {
    assert (h2ServerStatus == CREATED || h2ServerStatus == RUNNING)

    if (h2ServerStatus == CREATED) try {
      logger.warn("Starting H2 TCP server")
      h2Server.start()
      h2ServerStatus = RUNNING
    } catch { case e: Throwable =>
      logger.warn(s"Error starting H2 TCP server: ${ e.getMessage }")
    }

    try { // In case the last session did not clean up
      logger.warn(s"Creating H2 database from $resourcePath or $fallbackPath")
      processEvolution.downs(SelectedCtx)
    } catch { case e: Throwable =>
      logger.warn(e.getMessage)
    }
    processEvolution.ups(SelectedCtx)
    logger.warn("H2 embedded database should exist now.")
  }

  override def afterAll(): Unit = {
    super.afterAll()

    if (h2ServerStatus == RUNNING) {
      logger.warn(s"Deleting H2 database")
      processEvolution.downs(SelectedCtx)

      logger.warn("Stopping H2 TCP server")
      h2Server.stop()
      h2ServerStatus = CREATED
    }
  }
}
