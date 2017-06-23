package model.persistence

import com.typesafe.config.{Config, ConfigFactory}
import io.getquill._
import io.getquill.context.jdbc.JdbcContext
import io.getquill.context.sql.idiom.SqlIdiom
import scala.concurrent.duration.Duration

/** Selects the appropriate Quill JdbcContext at runtime according to the database configuration in `reference.conf`,
  * or `application.conf`, if provided.
  * @see [[http://getquill.io/#contexts-sql-contexts Quill SQL Contexts]] */
trait QuillConfiguration {
  type AllDialects = H2Dialect with MySQLDialect with PostgresDialect with SqliteDialect
  type Ctx = JdbcContext[_ >: AllDialects <: SqlIdiom, TableNameSnakeCase]

  protected lazy val config: Config = ConfigFactory.load.getConfig("persistence-config")

  protected lazy val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)

  protected lazy val dbType: String = config.getString("use")

  lazy val ctx = try {
    new PostgresJdbcContext[TableNameSnakeCase]("postgres")
  /*dbType match {  // TODO make this work somehow
    case "h2"       => new H2JdbcContext[TableNameSnakeCase](s"persistence-config.$dbType")
    case "mysql"    => new MysqlJdbcContext[TableNameSnakeCase](s"persistence-config.$dbType")
    case "postgres" => new PostgresJdbcContext[TableNameSnakeCase](s"persistence-config.$dbType")
    case "sqlite"   => new SqliteJdbcContext[TableNameSnakeCase](s"persistence-config.$dbType")
    case _          => throw new Exception("No database configured.")
  }*/
  } catch {
    case e: Throwable =>
//      println(new RichThrowable(e).format())
      println(e.getMessage)
      throw e
  }
}

object QuillConfiguration extends QuillConfiguration
