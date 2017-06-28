# Cached Persistence
[![Build Status](https://travis-ci.org/mslinn/quill-cache.svg?branch=master)](https://travis-ci.org/mslinn/quill-cache)
[![GitHub version](https://badge.fury.io/gh/mslinn%2Fquill-cache.svg)](https://badge.fury.io/gh/mslinn%2Fquill-cache)

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
[CachedPersistence](http://github.com/mslinn/quill-cache/latest/api/#model.persistence.CachedPersistence) 
abstract class.

You are free to name DAOs anything you like; this library does not mandate any naming convention.
Scala DAOs are often given the same name as the class that they persist, but with a suffix indicating plurality.
For example, if a case class named `Point` needs to be persisted, the DAO is usually called `Points`.
Unlike some other persistence libraries for Scala, Quill allows you to define your DAO in the case class's companion object,
so you also have that option when using this library.

This library provides each DAO with its own cache.
DAOs that extend `CachedPersistence` have a method called
[preload()](http://mslinn.github.io/quill-cache/latest/api/index.html#model.persistence.CacheLike@preload:List[CaseClass])
which your application's initialization must invoke in order to fill that DAO's cache.
A cache can be flushed by calling the DAO's 
[flushCache()](http://blog.mslinn.com/quill-cache/latest/api/index.html#model.persistence.CacheLike@flushCache():Unit) method.
Because `preload()` always flushes the cache before loading it you probably won't ever need to explicitly call `flushCache()`.

## Cache Types
Two types of caches are supported by `CachedPersistence`: 
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

    libraryDependencies += "com.micronautics" %% "quill-cache" % "3.0.2" withSources()
    
You will also need to add a driver for the database you are using.
Quill only supports H2, MySQL, Postgres and Sqlite.
For example, for Postgres, add:

    libraryDependencies += "org.postgresql" % "postgresql" % "42.1.1"

You will need a logging framework. Logback is a good choice:

    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

## Configuration
Your database configuration is specified by a file called `application.conf` on the classpath.
Please see `src/main/scala/resources/reference.conf` for an example of how to set that up. 

Here is an excerpt:

```
quill-cache {
  use: h2
  timeout: 1 minute

  # See https://github.com/getquill/quill/blob/master/quill-jdbc/src/test/resources/application.conf
  # See https://github.com/brettwooldridge/HikariCP#initialization
  # See https://github.com/brettwooldridge/HikariCP/blob/master/src/main/java/com/zaxxer/hikari/HikariConfig.java#L63-L97
  # See https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing#the-formula

  h2 {
    dataSourceClassName = org.h2.jdbcx.JdbcDataSource
    dataSource {
      url = "jdbc:h2:mem:default"
      user = sa
      password = ""
    }
  }

  postgres {
    connectionTimeout = 10000
    dataSource {
      databaseName = ${?DB}
      password = ${?PGPASSWORD}
      serverName = ${?PGSERVER}
      ssl = true
      sslfactory = "org.postgresql.ssl.NonValidatingFactory"
      user = ${?USERID}
    }
    dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
  }
}
```

The `quill-cache` section specifies parameters for this library:
  * `use` indicates the name of a subsection containing the active database configuration.
    The other database configuration subsections are ignored.
    Only two are shown above (`h2` and `postgres`), but you can make up your own subsections and call them whatever you want.
    The supplied `reference.conf` file also has a sample MySQL section.
  * `timeout` indicates how long a database query is allowed to run before an error is declared.
  * The contents of the named subsections are database-dependent.
  * [Hikari](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby) interprets the meaning of `datSource` sections.

## Sample Code
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
