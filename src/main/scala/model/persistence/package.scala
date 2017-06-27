package model

import persistence._

/** Scala uses case classes for modeling domain objects.
  * `quill-cache` optimizes database access for read-mostly domain objects by providing a caching layer overtop
  * [[https://github.com/getquill/quill Quill]].
  * This library depends on [[https://github.com/mslinn/has-id has-id]], and case classes that need to be cached must extend
  * [[http://mslinn.github.io/has-id/latest/api/#model.persistence.HasId HasId]].
  * `HasId` is generic and quite flexible, so you are encouraged to subclass all your domain objects from `HasId`,
  * even if they do not require database caching.
  *
  * The current version of this library has no provision for distributed caches.
  * This could be retrofitted, however the author did not have the need, so the work was not done.
  *
  * <h2>DAOs</h2>
  * The [[https://en.wikipedia.org/wiki/Data_access_object data access object pattern]] (DAO) is common across all computer languages.
  * DAOs for case classes that require database caching must extend the [[CachedPersistence]] abstract class.
  *
  * You are free to name DAOs anything you like; this library does not mandate any naming convention.
  * Scala DAOs are often given the same name as the class that they persist, but with a suffix indicating plurality.
  * For example, if a case class named `Point` needs to be persisted, the DAO is usually called `Points`.
  * Unlike some other persistence libraries for Scala, Quill allows you to define your DAO in the case class's companion object,
  * so you also have that option when using this library.
  *
  * This library provides each DAO with its own cache.
  * DAOs that extend `CachedPersistence` have a method called `preload()`
  * which your application's initialization must invoke in order to fill that DAO's cache.
  * A cache can be flushed by calling the DAO's `flushCache()` method.
  * Because `preload()` always flushes the cache before loading it you probably won't ever need to explicitly call `flushCache()`.
  *
  * <h2>Cache Types</h2>
  * Two types of caches are supported by `CachedPersistence`:
  *   - [[http://mslinn.github.io/scalacourses-utils/latest/api/com/micronautics/cache/StrongCache.html StrongCache]],
  *     which is locked into memory until the cache is explicitly flushed.
  *     Mix the [[http://mslinn.github.io/quill-cache/latest/api/#model.persistence.StrongCacheLike StrongCacheLike]]
  *     trait into the DAO to provide this behavior.
  *     This type of cache is useful when there is enough memory to hold all instances of the case class.
  *   - [[http://mslinn.github.io/scalacourses-utils/latest/api/com/micronautics/cache/SoftCache.html SoftCache]],
  *      which contains "soft" values that might expire by timing out or might get bumped if memory fills up.
  *      Mix the [[http://mslinn.github.io/quill-cache/latest/api/#model.persistence.SoftCacheLike SoftCacheLike]]
  *      trait into the DAO to provide this behavior.
  *      DAOs that mix in `SoftCacheLike` do not assume that all instances of the case class can fit into memory.
  *      `SoftCacheLike` finders query the database after every cache miss.
  *      Because of this, `SoftCacheLike` finders run more slowly than `StrongCacheLike` finders when the cache does not contain the desired value.
  *      This trait is experimental, do not use in production.
  *
  * <h2>Consistent APIs for Cached and Uncached DAOs</h2>
  * `CachedPersistence` subclasses [[UnCachedPersistence]],
  * which you can use to derive DAOs for case classes that must have direct access to the database so the case classes are not cached.
  * You don't have to subclass `UnCachedPersistence` to get this behavior, but if you do then the DAOs for your cached
  * domain objects will have the same interface as the DAOs for your uncached domain objects,
  * and your code's structure will be more consistent.
  *
  * <h2>Configuration</h2>
  * Your database configuration is specified by a file called `application.conf` on the classpath.
  * Please see `src/main/scala/resources/reference.conf` for an example of how to set that up.
  *
  * Here is an excerpt:
  *
  * {{{
  * quill-cache {
  *   use: h2
  *   timeout: 1 minute
  *
  *   h2 {
  *     dataSourceClassName = org.h2.jdbcx.JdbcDataSource
  *     dataSource {
  *       url = "jdbc:h2:mem:default"
  *       user = sa
  *       password = ""
  *     }
  *   }
  *
  *   postgres {
  *     connectionTimeout = 10000
  *     dataSource {
  *       databaseName = ${?DB}
  *       password = ${?PGPASSWORD}
  *       serverName = ${?PGSERVER}
  *       ssl = true
  *       sslfactory = "org.postgresql.ssl.NonValidatingFactory"
  *       user = ${?USERID}
  *     }
  *     dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
  *   }
  * }
  * }}}
  *
  * The `quill-cache` section specifies parameters for this library:
  *   - `use` indicates the name of a subsection containing detailed database configuration.
  *     The other database configuration subsections are ignored.
  *     Only three are provided (`h2`, `mysql` and `postgres`), but you can make up your own subsections and call them whatever you want.
  *   - `timeout` indicates how long a database query is allowed to run before an error is declared.
  *   - The contents of the named subsections are database dependent.
  *   - [[https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby Hikari]] interprets the meaning of this section.
  *
  * See also [[https://github.com/getquill/quill/blob/master/quill-jdbc/src/test/resources/application.conf the Quill test application.conf]],
  * [[https://github.com/brettwooldridge/HikariCP#initialization Hikari initialization]] and
  * [[https://github.com/brettwooldridge/HikariCP/blob/master/src/main/java/com/zaxxer/hikari/HikariConfig.java#L63-L97 HikariConfig.java]] */
package object persistence {
  implicit class RichThrowable(throwable: Throwable) {
    def format(asHtml: Boolean=false, showStackTrace: Boolean = false): String =
      new Throwables{}.format(throwable, asHtml, showStackTrace)
  }
}

package persistence {
  trait Throwables {
    /** @param showStackTrace is overridden if the Exception has no cause and no message */
    def format(ex: Throwable, asHtml: Boolean=false, showStackTrace: Boolean = false): String = {
      val cause = ex.getCause
      val noCause = (null==cause) || cause.toString.trim.isEmpty

      val message = ex.getMessage
      val noMessage = (null==message) || message.trim.isEmpty

      (if (noCause) "" else s"$cause: ") + (if (noMessage) "" else message) +
        (if (asHtml) {
          if (showStackTrace || (noCause && noMessage))
            "\n<pre>" + ex.getStackTrace.mkString("\n  ", "\n  ", "\n") + "</pre>\n"
          else ""
        } else { // console output
          (if (!showStackTrace && (!noCause || !noMessage)) "" else "\n  ") +
          (if (showStackTrace || (noCause && noMessage)) ex.getStackTrace.mkString("\n  ") else "")
        })
    }
  }
}
