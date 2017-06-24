package model.persistence

import com.typesafe.config.{Config, ConfigFactory}
import io.getquill._
import scala.concurrent.duration.Duration
import scala.reflect.runtime.universe._

protected sealed class DbWitness[T: TypeTag](val ctx: T)
class H2Witness[N](configPrefix: String)       extends DbWitness(new H2JdbcContext[N](configPrefix))
/* Error:(9, 56) No TypeTag available for io.getquill.H2JdbcContext[N]
   Error:(9, 56) not enough arguments for constructor DbWitness: (implicit evidence$1: reflect.runtime.universe.TypeTag[io.getquill.H2JdbcContext[N]])model.persistence.DbWitness[io.getquill.H2JdbcContext[N]].
  Unspecified value parameter evidence$1. */
class MysqlWitness[N](configPrefix: String)    extends DbWitness(new MysqlJdbcContext[N](configPrefix))
class PostgresWitness[N](configPrefix: String) extends DbWitness(new PostgresJdbcContext[N](configPrefix))
class SqliteWitness[N](configPrefix: String)   extends DbWitness(new SqliteJdbcContext[N](configPrefix))

object QuillConfiguration {
  type AllDialects = H2Dialect with MySQLDialect with PostgresDialect with SqliteDialect
  type AllContexts[N] = H2JdbcContext[N] with MysqlJdbcContext[N] with PostgresJdbcContext[N] with SqliteJdbcContext[N]
  protected lazy val config: Config = ConfigFactory.load.getConfig("persistence-config")

  protected lazy val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)

  protected lazy val dbType: String = config.getString("use")

  // What is the return type of dbWitness?
  def dbWitness[N <: NamingStrategy] = dbType match {
    case "h2"       => new H2Witness[N](s"persistence-config.$dbType")
    case "mysql"    => new MysqlWitness[N](s"persistence-config.$dbType")
    case "postgres" => new PostgresWitness[N](s"persistence-config.$dbType")
    case "sqlite"   => new SqliteWitness[N](s"persistence-config.$dbType")
    case _          => throw new Exception("No database configured.")
  }
}
