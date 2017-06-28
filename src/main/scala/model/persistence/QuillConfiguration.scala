package model.persistence

/** Computes a property called `ctx`, which is the Quill context.
  * Its type varies according to the database selected, which complicates things.
  * Seems no type can be ascribed to `ctx` such that importing it will define the encoders and decoders.
  * @see [[http://getquill.io/#quotation-introduction The Quill docs]]. */
object QuillConfiguration extends ConfigParse {
  import io.getquill._

  protected type N = TableNameSnakeCase

  // TODO write a Scala macro to automagically select the correct holder, instead of hard-coding it like this
  lazy val ctx              = new PostgresJdbcContext[N](configPrefix)
  lazy val ctxH2            = new H2JdbcContext[N](configPrefix)
  lazy val ctxMySQL         = new MysqlJdbcContext[N](configPrefix)
  lazy val ctxPostgres      = new PostgresJdbcContext[N](configPrefix)
  lazy val ctxPostgresAsync = new PostgresAsyncContext[N](configPrefix)
  lazy val ctxSqlite        = new SqliteJdbcContext[N](configPrefix)

  /* The macro needs to do the following, and in particular set ctx to an untyped value
  val ctx = dbType match {
    case "h2"             => new H2JdbcContext[N](configPrefix)
    case "mysql"          => new MysqlJdbcContext[N](configPrefix)
    case "postgres"       => new PostgresJdbcContext[N](configPrefix)
    case "postgres_async" => new PostgresAsyncContext[N](configPrefix)
    case "sqlite"         => new SqliteJdbcContext[N](configPrefix)
    case x                => throw new Exception(s"Error: '$x' is an invalid database type. No database configured.")
  } */
}
