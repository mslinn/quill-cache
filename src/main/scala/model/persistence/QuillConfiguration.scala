package model.persistence

import com.typesafe.config.{Config, ConfigFactory}
import io.getquill._
import io.getquill.context.jdbc.JdbcContext
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

protected sealed class DbWitnesses[T: ClassTag](ctx: T)
class H2Witness[N](configPrefix: String)       extends DbWitnesses(new H2JdbcContext[N](configPrefix))
class MysqlWitness[N](configPrefix: String)    extends DbWitnesses(new MysqlJdbcContext[N](configPrefix))
class PostgresWitness[N](configPrefix: String) extends DbWitnesses(new PostgresJdbcContext[N](configPrefix))
class SqliteWitness[N](configPrefix: String)   extends DbWitnesses(new SqliteJdbcContext[N](configPrefix))

object QuillConfiguration {
  type AllDialects = H2Dialect with MySQLDialect with PostgresDialect with SqliteDialect
  type AllContexts[N] = H2JdbcContext[N] with MysqlJdbcContext[N] with PostgresJdbcContext[N] with SqliteJdbcContext[N]
  protected lazy val config: Config = ConfigFactory.load.getConfig("persistence-config")

  protected lazy val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)

  protected lazy val dbType: String = config.getString("use")

  def ctx[N <: NamingStrategy] = dbType match {
    case "h2"       => new H2Witness[N](s"persistence-config.$dbType")
    case "mysql"    => new MysqlWitness[N](s"persistence-config.$dbType")
    case "postgres" => new PostgresWitness[N](s"persistence-config.$dbType")
    case "sqlite"   => new SqliteWitness[N](s"persistence-config.$dbType")
    case _          => throw new Exception("No database configured.")
  }
}
