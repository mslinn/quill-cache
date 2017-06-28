package model.persistence

/** Computes a property called `ctx`, which is the Quill context.
  * Its type varies according to the database selected, which complicates things.
  * Seems no type can be ascribed to `ctx` such that importing it will define the encoders and decoders.
  * @see [[http://getquill.io/#quotation-introduction The Quill docs]]. */
object QuillConfiguration extends ConfigParse {
  import io.getquill._

  protected type N = TableNameSnakeCase

  // TODO automagically select the correct holder, instead of hard-coding it like this
  lazy val ctx              = new PostgresJdbcContext[N](configPrefix)
  lazy val ctxH2            = new H2JdbcContext[N](configPrefix)
  lazy val ctxMySQL         = new MysqlJdbcContext[N](configPrefix)
  lazy val ctxPostgres      = new PostgresJdbcContext[N](configPrefix)
  lazy val ctxPostgresAsync = new PostgresAsyncContext[N](configPrefix)
  lazy val ctxSqlite        = new SqliteJdbcContext[N](configPrefix)
}
