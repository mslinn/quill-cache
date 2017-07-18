package model

import H2ServerStatus._
import com.typesafe.config.Config
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

/** Singleton that controls the H2 Postgres-compatible TCP server */
case object H2Server extends ConfigParse {
  logger.warn("Creating H2 Postgres-compatible server")
  protected val h2Config: Config = config.getConfig("h2")
  protected val dataSource: Config = h2Config.getConfig("dataSource")
  protected val url: String = dataSource.getString("url")
  protected val h2Server: Server = org.h2.tools.Server.createTcpServer("-baseDir", "./h2data")
  var state = CREATED

  def start(): Unit = if (state==CREATED) {
    try {
      logger.warn("Starting H2 server")
      h2Server.start()
      state = RUNNING
    } catch {
      case e: Throwable =>
        logger.warn(s"Error starting H2 server: ${ e.format() }\n" + e.getStackTrace.mkString("\n"))
    }
  } else {
    logger.warn("H2Server.start() WARNING - start() ignored, already running")
  }

  def stop(processEvolution: ProcessEvolution): Unit =
    if (true) {
      logger.warn("H2 stop request ignored because it does not work right.")
      if (state == RUNNING) {
        logger.warn(s"Deleting H2 database tables")
        processEvolution.downs(SelectedCtx)
      }
    } else if (state == RUNNING) {
      logger.warn("Stopping H2 server")
      h2Server.stop()
      state = CREATED
    } else
      logger.warn("H2Server.stop() WARNING - stop() ignored, already stopped")
 }

trait LocalH2Server {
  if (H2Server.state == CREATED) H2Server.start()
}

// Because BeforeAndAfterAll invokes super.run, mix this trait in last
trait SetupTeardown extends BeforeAndAfterAll { this: WordSpec with LocalH2Server =>
  val resourcePath = "evolutions/default/1.sql" // for accessing evolution file as a resource from a jar
  val fallbackPath = s"src/test/resources/$resourcePath" // for testing this project
  val processEvolution = new ProcessEvolution(resourcePath, fallbackPath)

  override def beforeAll(): Unit = {
    assert (H2Server.state == CREATED || H2Server.state == RUNNING)
    H2Server.start()

    try { // In case the last session did not clean up
      logger.warn(s"Creating H2 database tables from $resourcePath or $fallbackPath")
      processEvolution.downs(SelectedCtx)
    } catch { case e: Throwable =>
      logger.warn(e.getMessage)
    }
    processEvolution.ups(SelectedCtx)
    logger.warn("H2 database tables should exist now.")
  }

  override def afterAll(): Unit = {
    super.afterAll()
    H2Server.stop(processEvolution)
  }
}
