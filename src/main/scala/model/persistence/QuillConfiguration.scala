package model.persistence

import io.getquill._

trait CtxLike {
  import io.getquill.context.async.AsyncContext
  import io.getquill.context.jdbc.JdbcContext

  val ctx: JdbcContext[_, _] /*with AsyncContext[_, _, _]*/
}

/** Mix this trait into any class that needs to access the H2 synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends H2Ctx {
  *   import ctx._
  * }
  * }}}
  * The [[H2Configuration]] object mixes in this trait and provides an alternative mechanism. */
trait H2Ctx extends ConfigParse with CtxLike {
  lazy val ctx = new H2JdbcContext[TableNameSnakeCase](configPrefix("h2"))
}

/** Mix this trait into any class that needs to access the MySQL synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends MySqlCtx {
  *   import ctx._
  * }
  * }}}
  * The [[MysqlConfiguration]] object mixes in this trait and provides an alternative mechanism. */
trait MySqlCtx extends ConfigParse with CtxLike {
  lazy val ctx = new MysqlJdbcContext[TableNameSnakeCase](configPrefix("mysql"))
}

/** Mix this trait into any class that needs to access the MySQL asynchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends MysqlAsyncCtx {
  *   import ctx._
  * }
  * }}}
  * The [[MysqlAsyncConfiguration]] object mixes in this trait and provides an alternative mechanism. */
//trait MysqlAsyncCtx extends ConfigParse with CtxLike {
//  lazy val ctx = new MysqlAsyncContext[TableNameSnakeCase](configPrefix("mysql-async"))
//}

/** Mix this trait into any class that needs to access the Postgres synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends PostgresCtx {
  *   import ctx._
  * }
  * }}}
  * The [[PostgresConfiguration]] object mixes in this trait and provides an alternative mechanism. */
trait PostgresCtx extends ConfigParse with CtxLike {
  lazy val ctx = new PostgresJdbcContext[TableNameSnakeCase](configPrefix("postgres"))
}

/** Mix this trait into any class that needs to access the Postgres asynchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends PostgresAsyncCtx {
  *   import ctx._
  * }
  * }}}
  * The [[PostgresAsyncConfiguration]] object mixes in this trait and provides an alternative mechanism. */
//trait PostgresAsyncCtx extends ConfigParse with CtxLike {
//  lazy val ctx = new PostgresAsyncContext[TableNameSnakeCase](configPrefix("postgres-async"))
//}

/** Mix this trait into any class that needs to access the Sqlite synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends SqliteCtx {
  *   import ctx._
  * }
  * }}}
  * The [[SqliteConfiguration]] object mixes in this trait and provides an alternative mechanism. */
trait SqliteCtx extends ConfigParse with CtxLike {
  lazy val ctx = new SqliteJdbcContext[TableNameSnakeCase](configPrefix("sqlite"))
}

/** Sample object for exposing the Postgres synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import QuillConfiguration.ctx._}}}
  * You can define your own object if you need to use a different database
  * The type of `ctx` varies according to the database selected, so no type can be ascribed to `ctx`,
  * otherwise the Quill encoders and decoders won't be recognized.
  * @see [[http://getquill.io/#quotation-introduction The Quill docs]]. */
object QuillConfiguration extends PostgresCtx

/** Object for exposing the H2 synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import H2Configuration.ctx._}}} */
object H2Configuration extends H2Ctx

/** Object for exposing the MySQL synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import MysqlConfiguration.ctx._}}} */
object MysqlConfiguration extends MySqlCtx

/** Object for exposing the MySQL asynchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import MysqlAsyncConfiguration.ctx._}}} */
//object MysqlAsyncConfiguration extends MysqlAsyncCtx

/** Object for exposing the Postgres synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import PostgresConfiguration.ctx._}}} */
object PostgresConfiguration extends PostgresCtx

/** Object for exposing the Postgres asynchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import PostgresAsyncConfiguration.ctx._}}} */
//object PostgresAsyncConfiguration extends PostgresAsyncCtx

/** Object for exposing the Sqlite synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import SqliteConfiguration.ctx._}}} */
object SqliteConfiguration extends SqliteCtx
