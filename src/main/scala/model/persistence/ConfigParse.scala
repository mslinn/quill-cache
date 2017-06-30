package model.persistence

import ai.x.safe._

/** Parse `application.conf` or `reference.conf` for database parameters */
trait ConfigParse {
  import com.typesafe.config.{Config, ConfigFactory}
  import scala.concurrent.duration.Duration

  val config: Config = ConfigFactory.load.getConfig("quill-cache")
  val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)

  def configPrefix(dbType: String): String = safe"quill-cache.$dbType"
}
