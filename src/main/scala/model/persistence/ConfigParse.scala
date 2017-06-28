package model.persistence

/** Parse `application.conf` or `reference.conf` for database parameters */
trait ConfigParse {
  import com.typesafe.config.{Config, ConfigFactory}
  import scala.concurrent.duration.Duration

  val quillSection = "quill-cache"
  val config: Config = ConfigFactory.load.getConfig(quillSection)
  val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)
  val dbType: String = config.getString("use")
  val configPrefix: String = s"$quillSection.$dbType"
}
