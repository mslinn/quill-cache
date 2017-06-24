package model.persistence

import com.typesafe.config.{Config, ConfigFactory}
import io.getquill._
import scala.concurrent.duration.Duration
import scala.reflect.runtime.universe._

sealed class DbWitness[T: TypeTag](val ctx: T)
class H2Witness(configPrefix: String)       extends DbWitness(new H2JdbcContext[TableNameSnakeCase](configPrefix))
class MysqlWitness(configPrefix: String)    extends DbWitness(new MysqlJdbcContext[TableNameSnakeCase](configPrefix))
class PostgresWitness(configPrefix: String) extends DbWitness(new PostgresJdbcContext[TableNameSnakeCase](configPrefix))
class SqliteWitness(configPrefix: String)   extends DbWitness(new SqliteJdbcContext[TableNameSnakeCase](configPrefix))

object QuillConfiguration {
  type AllDialects = H2Dialect with MySQLDialect with PostgresDialect with SqliteDialect
  type AllContexts = H2JdbcContext[TableNameSnakeCase] with MysqlJdbcContext[TableNameSnakeCase] with
                     PostgresJdbcContext[TableNameSnakeCase] with SqliteJdbcContext[TableNameSnakeCase]
  protected lazy val quillSection = "quill-cache"
  protected lazy val config: Config = ConfigFactory.load.getConfig(quillSection)

  protected lazy val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)

  protected lazy val dbType: String = config.getString("use")

  // TODO What type can be ascribed to dbWitness such that importing it will define the encoders and decoders?
  def dbWitness = try {
    val configPrefix = s"$quillSection.$dbType"
    dbType match {
      case "h2"       => new H2Witness(configPrefix)
      case "mysql"    => new MysqlWitness(configPrefix)
      case "postgres" => new PostgresWitness(configPrefix)
      case "sqlite"   => new SqliteWitness(configPrefix)
      case _          => throw new Exception("No database configured.")
    }
  } catch {
    case e: Throwable =>
      println(e.getMessage)
      throw e
  }
}
