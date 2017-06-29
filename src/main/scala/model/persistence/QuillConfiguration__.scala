package model.persistence

import io.getquill._
import scala.language.dynamics

/** Another failed experiment */
object QuillConfiguration__ extends ConfigParse {
  protected type N = TableNameSnakeCase

  sealed abstract class DynType[A] {
    def exec(dbType: String, contextType: A): A
  }

  implicit object H2Type extends DynType[H2JdbcContext[N]] {
    def exec(configPrefix: String, contextType: H2JdbcContext[N]): H2JdbcContext[N] = contextType
  }

  implicit object MysqlType extends DynType[MysqlJdbcContext[N]] {
    def exec(configPrefix: String, contextType: MysqlJdbcContext[N]): MysqlJdbcContext[N] = contextType
  }

  implicit object PostgresType extends DynType[PostgresJdbcContext[N]] {
    def exec(configPrefix: String, contextType: PostgresJdbcContext[N]): PostgresJdbcContext[N] = contextType
  }

  implicit object PostgresAsyncType extends DynType[PostgresAsyncContext[N]] {
    def exec(configPrefix: String, contextType: PostgresAsyncContext[N]): PostgresAsyncContext[N] = contextType
  }

  implicit object SqliteType extends DynType[SqliteJdbcContext[N]] {
    def exec(configPrefix: String, contextType: SqliteJdbcContext[N]): SqliteJdbcContext[N] = contextType
  }

  val dynImpl = new DynImpl
  val ctx = dynImpl.applyDynamic(dbType)
}

class DynImpl extends Dynamic {
  import model.persistence.QuillConfiguration._

  protected type N = TableNameSnakeCase

  def applyDynamic(configPrefix: String) = {
    dbType match {
      case "h2"             => new H2JdbcContext[N](configPrefix)
      case "mysql"          => new MysqlJdbcContext[N](configPrefix)
      case "postgres"       => new PostgresJdbcContext[N](configPrefix)
      case "postgres_async" => new PostgresAsyncContext[N](configPrefix)
      case "sqlite"         => new SqliteJdbcContext[N](configPrefix)
      case x                => throw new Exception(s"Error: '$x' is an invalid database type. No database configured.")
    }
  }
}
