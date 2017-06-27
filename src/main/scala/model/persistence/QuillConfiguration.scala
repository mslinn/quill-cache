package model.persistence

/** Parse `application.conf` or `reference.conf` for database parameters */
trait ConfigParse {
  import com.typesafe.config.{Config, ConfigFactory}
  import scala.concurrent.duration.Duration

  val quillSection = "quill-cache"
  val config: Config = ConfigFactory.load.getConfig(quillSection)
  val dbTimeout: Duration = Duration.fromNanos(config.getDuration("timeout").toNanos)
  val dbType: String = config.getString("use")
  val configPrefix: String = s"$quillSection.$dbType"
}

/** Computes a property called `ctx`, which is the Quill context.
  * Its type varies according to the database selected, which complicates things.
  * Seems no type can be ascribed to `ctx` such that importing it will define the encoders and decoders.
  * @see [[http://getquill.io/#quotation-introduction The Quill docs]]. */
object QuillConfiguration extends ConfigParse {
  import io.getquill._
  import scala.language.{higherKinds, implicitConversions}
  import scala.reflect.runtime.universe
  import scala.reflect.runtime.universe._

  // If DbContextHolder had a TypeTag or ClassTag for A, would it somehow be possible to use that type to set the type of ctx at the bottom of this object?
  protected class DbContextHolder[A](val ctx: A)

  protected object DbContextHolder {
    def apply[A](ctx: A) = new DbContextHolder(ctx)
  }

  protected type N = TableNameSnakeCase
  protected implicit val h2Holder       = DbContextHolder(new H2JdbcContext[N](configPrefix))
  protected implicit val mySqlHolder    = DbContextHolder(new MysqlJdbcContext[N](configPrefix))
  protected implicit val postgresHolder = DbContextHolder(new PostgresJdbcContext[N](configPrefix))
  protected implicit val sqliteHolder   = DbContextHolder(new SqliteJdbcContext[N](configPrefix))

  /** Convert from a dialect type to a DbContextHolder */
  // TODO not used - not even sure if this is helpful
  protected implicit def selectHolder(dialect: universe.Type): DbContextHolder[_] = dialect match {
    case _: H2Dialect       => h2Holder
    case _: MySQLDialect    => mySqlHolder
    case _: PostgresDialect => postgresHolder
    case _: SqliteDialect   => sqliteHolder
  }

  // this works properly
  protected val dialect: universe.Type = try {
    dbType match {
      case "h2"       => typeOf[H2Dialect]
      case "mysql"    => typeOf[MySQLDialect]
      case "postgres" => typeOf[PostgresDialect]
      case "sqlite"   => typeOf[SqliteDialect]
      case x          => throw new Exception(s"Error: '$x' is an invalid database type. No database configured.")
    }
  } catch {
    case e: Throwable =>
      println(e.getMessage)
      throw e
  }

  // TODO implicitly select the correct holder, instead of hard-coding it like this. Might selectHolder(dialect) help?
  val ctx = postgresHolder.ctx
  val ctxH2 = h2Holder.ctx
  val ctxMySQL = mySqlHolder.ctx
  val ctxPostgres = postgresHolder.ctx
  val ctxSqlite = sqliteHolder.ctx
}
