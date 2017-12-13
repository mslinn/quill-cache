<img src='https://raw.githubusercontent.com/mslinn/quill-cache/media/quill-cache.jpg' align='right' width='33%'>

# Cached Persistence for Quill
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/mslinn/quill-cache.svg?branch=master)](https://travis-ci.org/mslinn/quill-cache)
[![GitHub version](https://badge.fury.io/gh/mslinn%2Fquill-cache.svg)](https://badge.fury.io/gh/mslinn%2Fquill-cache)

## Features and Benefits
  * Dramatically reduces time to fetch results from read-mostly database tables
  * Database-independent [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) API 
    (`insert`, `deleteById`, `remove`, `update`, `upsert`, `zap`, `findAll`, `findById`, plus application-specific finders)
  * Thin, light type-safe API
  * Provides compatible interface to read-write database tables
  * Multiple databases can be configured, with configurations for development, testing, production, etc.
  * Choice of caching strategy (strong, soft or none)
  * Very little boilerplate (convention over configuration)
  * Switching databases only requires changing one word in a program
  * Play Framework evolution format support
  * ScalaTest unit test setup
  * [DAO code generator available](https://github.com/mslinn/quill-gen)

## Background
Scala uses case classes for modeling domain objects.
`quill-cache` optimizes database access for read-mostly domain objects by providing a caching layer overtop
[Quill](https://github.com/getquill/quill).
This library depends on [has-id](https://github.com/mslinn/has-id), and case classes that need to be cached must extend
[HasId](http://mslinn.github.io/has-id/latest/api/#model.persistence.HasId).
`HasId` is generic and quite flexible, so you are encouraged to subclass all your domain objects from `HasId`,
even if they do not require database caching.

The current version of this library has no provision for distributed caches.
This could be retrofitted, however the author did not have the need, so the work was not done.

## DAOs
The [data access object pattern](https://en.wikipedia.org/wiki/Data_access_object) (DAO) is common across all computer languages.
DAOs for case classes that require database caching must extend the
[CachedPersistence](http://mslinn.github.io/quill-cache/latest/api/#model.persistence.CachedPersistence)
abstract class.

You are free to name DAOs anything you like; this library does not mandate any naming convention.
Scala DAOs are often given the same name as the class that they persist, but with a suffix indicating plurality.
For example, if a case class named `Point` needs to be persisted, the DAO might be called `Points`.
Unlike some other persistence libraries for Scala, Quill allows you to define your DAO in the case class's companion object,
so you also have that option when using this library.

This library can provide each DAO with its own cache.
DAOs that extend `CachedPersistence` have a method called
[preload()](http://mslinn.github.io/quill-cache/latest/api/index.html#model.persistence.CacheLike@preload:List[CaseClass])
which your application's initialization must invoke in order to fill that DAO's cache.
A cache can be flushed by calling the DAO's
[flushCache()](http://blog.mslinn.com/quill-cache/latest/api/index.html#model.persistence.CacheLike@flushCache():Unit) method.
Because `preload()` always flushes the cache before loading it you probably won't ever need to explicitly call `flushCache()`.

## Cache Types
Two types of caches are supported:

* [StrongCache](http://mslinn.github.io/scalacourses-utils/latest/api/com/micronautics/cache/StrongCache.html),
    which is locked into memory until the cache is explicitly flushed.
    Mix the [StrongCacheLike](http://mslinn.github.io/quill-cache/latest/api/#model.persistence.StrongCacheLike)
    trait into the DAO to provide this behavior.
    This type of cache is useful when there is enough memory to hold all instances of the case class.
* [SoftCache](http://mslinn.github.io/scalacourses-utils/latest/api/com/micronautics/cache/SoftCache.html),
     which contains "soft" values that might expire by timing out or might get bumped if memory fills up.
     Mix the [SoftCacheLike](http://mslinn.github.io/quill-cache/latest/api/#model.persistence.SoftCacheLike)
     trait into the DAO to provide this behavior.
     DAOs that mix in `SoftCacheLike` do not assume that all instances of the case class can fit into memory.
     `SoftCacheLike` finders that return at most one item from a query the database after every cache miss.
     These `SoftCacheLike` finders run more slowly than `StrongCacheLike` finders when the cache does not contain the desired value.
     `SoftCacheLike` finders that return a list of items must always query the database.
     This trait is experimental, do not use in production.

Caches require an [ExecutionContext](http://www.scala-lang.org/api/current/scala/concurrent/ExecutionContext.html),
and the unit tests provide one:
```
package model.dao

import model.persistence.CacheExecutionContext
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/** Just delegates to standard Scala ExecutionContext, you can make this do whatever you want */
object TestExecutionContext extends CacheExecutionContext {
  protected val ec: ExecutionContextExecutor = ExecutionContext.Implicits.global
  override def execute(runnable: Runnable): Unit = ec.execute(runnable)

  override def reportFailure(cause: Throwable): Unit = ec.reportFailure(cause)
}
```

## Consistent APIs for Cached and Uncached DAOs
`CachedPersistence` subclasses
[UnCachedPersistence](http://mslinn.github.io/quill-cache/latest/api/#model.persistence.UnCachedPersistence),
which you can use to derive DAOs for case classes that must have direct access to the database so the case classes are not cached.
You don't have to subclass `UnCachedPersistence` to get this behavior, but if you do then the DAOs for your cached
domain objects will have the same interface as the DAOs for your uncached domain objects,
and your code's structure will be more consistent.

## Installation
Add this to your project's `build.sbt`:

    resolvers += "micronautics/scala on bintray" at "http://dl.bintray.com/micronautics/scala"

    libraryDependencies += "com.micronautics" %% "quill-cache" % "3.5.2"

You will also need to add a driver for the database you are using.
Quill only supports H2, MySQL, Postgres and Sqlite.
For example, for Postgres, add:

    libraryDependencies += "org.postgresql" % "postgresql" % "42.1.4"

You will need a logging framework. Logback is a good choice:

    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

## Configuration
Your database configuration is specified by a HOCON file called `application.conf` on the classpath.
Please see `src/main/scala/resources/reference.conf` for an example of how to set that up.

Here is an excerpt showing configuration for H2 and Postgres databases.
Only one of these databases can be active per database context:

```
quill-cache {
  h2 {
    dataSourceClassName = org.h2.jdbcx.JdbcDataSource
    dataSource {
      url = "jdbc:h2:tcp://localhost/./h2data;DB_CLOSE_ON_EXIT=FALSE"
      url = ${?H2_URL}

      user = sa
      user = ${?H2_USER}

      password = ""
      password = ${?H2_PASSWORD}
    }
  }

  postgres {
    connectionTimeout = 30000
    dataSource {
      databaseName = ${?DB}
      password = ${?PGPASSWORD}

      portNumber = 5432
      portNumber = ${?PGPORT}

      serverName = localhost
      serverName = ${?PGHOST}

      ssl = true
      sslfactory = "org.postgresql.ssl.NonValidatingFactory"
      #url = ""

      user = postgres
      user = ${?USERID}
    }
    dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
    maximumPoolSize = 100
  }
}
```

The `quill-cache` section of the configuration file specifies parameters for this library:
You can make up your own subsections and call them whatever you want.
    The supplied `reference.conf` file also has sample MySQL sections for sync and async, plus an async Postgres section.
The contents of the named subsections are database-dependent.
[Hikari](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby) interprets the meaning of the `dataSource` sections.

See also the [Quill application.conf](https://github.com/getquill/quill/blob/master/quill-jdbc/src/test/resources/application.conf),
[HikariCP initialization docs](https://github.com/brettwooldridge/HikariCP#initialization),
[HikariConfig source code](https://github.com/brettwooldridge/HikariCP/blob/master/src/main/java/com/zaxxer/hikari/HikariConfig.java#L63-L97),
and the [Hikari pool sizing docs](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing#the-formula).

## Working with quill-cache
### Quill Contexts
Quill-cache provides many flavors of Quill contexts, one for each type of supported database driver.
Each context is exposed as an `abstract class`.
Available abstract classes are: `H2Ctx`, `MySqlCtx`, `PostgresCtx`, and `SqliteCtx`.
Subclass the appropriate `abstract class` for the type of database driver you need, like this:

    class MyClass extends model.persistence.H2Ctx

### Asynchronous Drivers
Asynchronous drivers are not currently supported by `quill-cache`, but there is an
[open issue for this enhancement](https://github.com/mslinn/quill-cache/issues/2).
If you have need for this, or if you are looking for a fairly easy F/OSS Scala project to burnish your resume with,
you might want to submit a pull request for this behavior (it would closely model the asynch code).
The database contexts `MysqlAsyncCtx` and `PostgresAsyncCtx` have already been written in anticipation of async support.

### Best Practice
Define a trait called `SelectedCtx`, and mix it into all your DAOs.
`SelectedCtx` merely extends the database context used in your application.
The `PersistenceTest` DAO in `test/scala/model/dao` follows this pattern:

    trait SelectedCtx extends model.persistence.H2Ctx

Now define your application's Quill context as a singleton, and mix in the predefined implicits for Quill-cache defined in `QuillCacheImplicits`.

```
package model

import model.dao.SelectedCtx
import persistence.QuillCacheImplicits

case object Ctx extends SelectedCtx with QuillCacheImplicits
```

If you have more implicits to mix in, define a trait in the same manner as `QuillCacheImplicits` and mix it in as well:

```
trait MyQuillImplicits { ctx: JdbcContext[_, _] =>
  // define Quill Decoders, Encoders and Mappers here
}
```

After adding in `MyQuillImplicits`, your revised application Quill context `Ctx` is now:

```
package model

import model.dao.SelectedCtx
import persistence.QuillCacheImplicits

case object Ctx extends SelectedCtx with QuillCacheImplicits with MyQuillImplicits
```

Now import the Quill context's internally defined implicits into your DAO's scope.
Here are two examples of how to do that, one for cached and one for uncached persistence.
Notice that `Users` and `Tokens` are singletons, which makes them easy to work with.
Here is `Users`, a DAO with a strong cache, which means it needs an `ExecutionContext` like `TestExecutionContext`,
which is in scope because it resides in the same package:
```
import model.{Ctx, User}
import model.persistence._

object Users extends CachedPersistence[Long, Option[Long], User]
    with StrongCacheLike[Long, Option[Long], User] {
  import Ctx._

  // DAO code for User goes here
}
```

Here is `Tokens`, a DAO without any cache, which means it does not need an `ExecutionContext`:
```
import model.{Ctx, Token}
import model.persistence._

object Tokens extends UnCachedPersistence[Long, Option[Long], Token] {
  import Ctx._

  // DAO code for Token goes here
}
```

### Multiple Database Contexts
For circumstances where more than one database contexts need to share the same HikariCP pool, first construct a context,
then other contexts can be created from the first context's `dataSource`. In the following example, a context for an H2 database
is created using the `Ctx.dataSource`:

    case object Ctx2 extends H2Ctx(Ctx.dataSource) with MySpecialImplicits

Note that the new context need not have the same implicit decoders, encoders or mappers as the original context.
See the `ContextTest` unit test for a working example.

Here is another variation:
```
/** This causes a new Hikari pool to be created */
object AuthCtx extends PostgresCtx with QuillCacheImplicits with IdImplicitLike

abstract class DerivedCtx(dataSource: DataSource with Closeable)
  extends PostgresCtx(dataSource) with QuillCacheImplicits with IdImplicitLike

/** Reuse the HikariCP pool from `AuthCtx` */
object Ctx extends DerivedCtx(AuthCtx.dataSource) with MySpecialImplicits
```

### Working with DAOs
`Quill-cache` automatically defines a read-only property for each DAO, called `className`.
This property is derived from the unqualified name of the case class persisted by the DAO.
For example, if `model.User` is being persisted, `className` will be `User`.

Each DAO needs the following CRUD-related functions defined:

  1. `_findAll`     &ndash; Quill query foundation - Encapsulates the Quill query that returns all instances of the case class from the database
  1. `_deleteById`  &ndash; Encapsulates the Quill query that deletes the instance of the case class with the given `Id` from the database
  1. `_findById`    &ndash; Encapsulates the Quill query that optionally returns the instance of the case class from the database with the given
                            `Id`, or `None` if not found.
  1. `_insert`      &ndash; Encapsulates the Quill query that inserts the given instance of the case class into the database, and returns the
                            case class as it was stored, including any auto-increment fields.
  1. `_update`      &ndash; Encapsulates the Quill query that updates the given instance of the case class into the database, and returns the entity.
                            Throws an Exception if the case class was not previously persisted.
### DAO CRUD
Here is an example of the CRUD-related functions, implemented in the DAO for `model.User` in the `quill-cache` unit test suite.
```
  @inline def _findAll: List[User] = run { quote { query[User] } }

  val queryById: IdOptionLong => Quoted[EntityQuery[User]] =
    (id: IdOptionLong) =>
      quote { query[User].filter(_.id == lift(id)) }

  val _deleteById: (IdOptionLong) => Unit =
    (id: IdOptionLong) => {
      run { quote { queryById(id).delete } }
      ()
    }

  val _findById: IdOptionLong => Option[User] =
    (id: Id[Option[Long]]) =>
      run { quote { queryById(id) } }.headOption

  val _insert: User => User =
    (user: User) => {
      val id: Id[Option[Long]] = try {
        run { quote { query[User].insert(lift(user)) }.returning(_.id) }
      } catch {
        case e: Throwable =>
          logger.error(e.getMessage)
          throw e
      }
      user.setId(id)
    }

  val _update: User => User =
    (user: User) => {
      run { queryById(user.id).update(lift(user)) }
      user
    }
```

With the above defined, `quill-cache` automatically provides the following CRUD-related methods for each DAO:
Only finders can take advantage of a cache, if present:
```
@inline def deleteById(id: Id[_IdType]): Unit
@inline override def findAll: List[User]
def findById(id: Id[_IdType]): Option[User]
@inline def insert(user: User): User
@inline def update(user: User): User
@inline def remove(user: User): Unit
@inline def upsert(user: User): User
@inline def zap(): Unit
```

See the unit tests for examples of how to use this library.

## Scaladoc
[Here](http://mslinn.github.io/quill-cache/latest/api/#model.persistence.package)

## Sponsor
This project is sponsored by [Micronautics Research Corporation](http://www.micronauticsresearch.com/),
the company that delivers online Scala and Play training via [ScalaCourses.com](http://www.ScalaCourses.com).
You can learn how this project works by taking the [Introduction to Scala](http://www.ScalaCourses.com/showCourse/40),
and [Intermediate Scala](http://www.ScalaCourses.com/showCourse/45) courses.

## License
This software is published under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
