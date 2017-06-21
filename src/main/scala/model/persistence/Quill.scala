package model.persistence

import com.typesafe.config.{Config, ConfigFactory}
import io.getquill._
import io.getquill.context.jdbc.JdbcContext
import io.getquill.context.sql.idiom.SqlIdiom
import scala.concurrent.duration.Duration

/** Quill-specific caching database interface.
  * @see [[http://getquill.io/#contexts-sql-contexts Quill SQL Contexts]] */
trait Quill {
  type AllDialects = H2Dialect with MySQLDialect with PostgresDialect with SqliteDialect
  type Ctx = JdbcContext[_ >: AllDialects <: SqlIdiom, TableNameSnakeCase]

  protected lazy val config: Config = ConfigFactory.load.getConfig("persistence-config")

  protected lazy val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)

  protected lazy val dbType: String = config.getString("use")
  lazy val ctx: JdbcContext[_ >: AllDialects <: SqlIdiom, TableNameSnakeCase] = dbType match {
    case "h2"       => new H2JdbcContext[TableNameSnakeCase](dbType)
    case "mysql"    => new MysqlJdbcContext[TableNameSnakeCase](dbType)
    case "postgres" => new PostgresJdbcContext[TableNameSnakeCase](dbType)
    case "sqlite"   => new SqliteJdbcContext[TableNameSnakeCase](dbType)
  }
}

object Quill extends Quill
