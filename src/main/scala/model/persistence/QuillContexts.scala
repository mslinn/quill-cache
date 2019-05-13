package model.persistence

import java.io.Closeable
import javax.sql.DataSource
import com.typesafe.config.Config
import io.getquill._
import io.getquill.util.LoadConfig

/** Mix this trait into any class that needs to access the H2 synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends H2Ctx {
  *   import ctx._
  * }
  * }}}
  * The [[H2Configuration]] object mixes in this trait and provides an alternative mechanism. */
abstract class H2Ctx(override val dataSource: DataSource with Closeable)
  extends H2JdbcContext(TableNameSnakeCase, dataSource) {

  def this(config: JdbcContextConfig) = this(config.dataSource)
  def this(config: Config) = this(JdbcContextConfig(config))
  def this() = this(LoadConfig(ConfigParse.configPrefix("h2")))
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
abstract class MySqlCtx(override val dataSource: DataSource with Closeable)
  extends MysqlJdbcContext(TableNameSnakeCase, ConfigParse.configPrefix("mysql")) {

  def this(config: JdbcContextConfig) = this(config.dataSource)
  def this(config: Config) = this(JdbcContextConfig(config))
  def this() = this(LoadConfig(ConfigParse.configPrefix("mysql")))
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
abstract class MysqlAsyncCtx(val dataSource: DataSource with Closeable)
  extends MysqlAsyncContext(TableNameSnakeCase, ConfigParse.configPrefix("mysql-async")) {

  def this(config: JdbcContextConfig) = this(config.dataSource)
  def this(config: Config) = this(JdbcContextConfig(config))
  def this() = this(LoadConfig(ConfigParse.configPrefix("mysql-async")))
}

/** Mix this trait into any class that needs to access the Postgres synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends PostgresCtx {
  *   import ctx._
  * }
  * }}}
  * The [[PostgresConfiguration]] object mixes in this trait and provides an alternative mechanism. */
abstract class PostgresCtx(override val dataSource: DataSource with Closeable)
  extends PostgresJdbcContext(TableNameSnakeCase, ConfigParse.configPrefix("postgres")) {

  def this(config: JdbcContextConfig) = this(config.dataSource)
  def this(config: Config) = this(JdbcContextConfig(config))
  def this() = this(LoadConfig(ConfigParse.configPrefix("postgres")))}

/** Mix this trait into any class that needs to access the Postgres asynchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends PostgresAsyncCtx {
  *   import ctx._
  * }
  * }}}
  * The [[PostgresAsyncConfiguration]] object mixes in this trait and provides an alternative mechanism. */
abstract class PostgresAsyncCtx(val dataSource: DataSource with Closeable)
  extends PostgresAsyncContext(TableNameSnakeCase, ConfigParse.configPrefix("postgres-async")) {

  def this(config: JdbcContextConfig) = this(config.dataSource)
  def this(config: Config) = this(JdbcContextConfig(config))
  def this() = this(LoadConfig(ConfigParse.configPrefix("postgres-async")))}

/** Mix this trait into any class that needs to access the Sqlite synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{
  * class MyClass extends SqliteCtx {
  *   import ctx._
  * }
  * }}}
  * The [[SqliteConfiguration]] object mixes in this trait and provides an alternative mechanism. */
abstract class SqliteCtx(override val dataSource: DataSource with Closeable)
  extends SqliteJdbcContext(TableNameSnakeCase, ConfigParse.configPrefix("sqlite")) {

  def this(config: JdbcContextConfig) = this(config.dataSource)
  def this(config: Config) = this(JdbcContextConfig(config))
  def this() = this(LoadConfig(ConfigParse.configPrefix("sqlite")))}


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
object MysqlAsyncConfiguration extends MysqlAsyncCtx

/** Object for exposing the Postgres synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import PostgresConfiguration.ctx._}}} */
object PostgresConfiguration extends PostgresCtx

/** Object for exposing the Postgres asynchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import PostgresAsyncConfiguration.ctx._}}} */
object PostgresAsyncConfiguration extends PostgresAsyncCtx

/** Object for exposing the Sqlite synchronous configuration.
  * Exposes a property called `ctx`, which is the Quill context.
  * To use, simply import the context, like this:
  * {{{import SqliteConfiguration.ctx._}}} */
object SqliteConfiguration extends SqliteCtx
