package model.persistence

/** Parse `application.conf` and/or `reference.conf` for database parameters */
trait ConfigParse {
  import com.typesafe.config.{Config, ConfigFactory}

  val config: Config = ConfigFactory.load.getConfig("quill-cache")

  def configPrefix(dbType: String): String = s"quill-cache.$dbType"
}

object ConfigParse extends ConfigParse
