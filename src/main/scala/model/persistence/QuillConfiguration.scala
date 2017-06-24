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
  protected lazy val config: Config = ConfigFactory.load.getConfig("persistence-config")

  protected lazy val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)

  protected lazy val dbType: String = config.getString("use")

  // What is the type of dbWitness?
  def dbWitness = dbType match {
    case "h2"       => new H2Witness(s"persistence-config.$dbType")
    case "mysql"    => new MysqlWitness(s"persistence-config.$dbType")
    case "postgres" => new PostgresWitness(s"persistence-config.$dbType")
    case "sqlite"   => new SqliteWitness(s"persistence-config.$dbType")
    case _          => throw new Exception("No database configured.")
  }
}
