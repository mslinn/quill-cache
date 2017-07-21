package model.persistence

import java.util.concurrent.{Callable, TimeUnit}
import com.google.common.cache.{Cache, CacheBuilder}
import scala.concurrent.Future

/** Features soft values that might expire */
class SoftCache[Key<:Any, Value<:Any](override val concurrencyLevel: Int=4, override val timeoutMinutes: Int=5)
                                     (implicit override val executionContext: CacheExecutionContext)
  extends AbstractCache[Key, Value](concurrencyLevel, timeoutMinutes) {

  lazy val underlying: Cache[Object, Object] = if (timeoutMinutes==0)
    CacheBuilder
      .newBuilder
      .concurrencyLevel(concurrencyLevel)
      .softValues
      .recordStats
      .build[Object, Object]
  else
    CacheBuilder
      .newBuilder
      .concurrencyLevel(concurrencyLevel)
      .softValues
      .expireAfterAccess(timeoutMinutes.toLong, TimeUnit.MINUTES)
      .recordStats
      .build[Object, Object]
}

object SoftCache {
  @inline def apply[Key<:Any, Value<:Any](concurrencyLevel: Int=4, timeoutMinutes: Int=5)
                                         (implicit executionContext: CacheExecutionContext): SoftCache[Key, Value] =
    new SoftCache[Key, Value](concurrencyLevel, timeoutMinutes) {}
}


class StrongCache[Key<:Any, Value<:Any](override val concurrencyLevel: Int=4, timeoutMinutes: Int=0)
                                       (implicit override val executionContext: CacheExecutionContext)
  extends AbstractCache[Key, Value](concurrencyLevel, timeoutMinutes) {

  lazy val underlying: Cache[Object, Object] = if (timeoutMinutes==0)
    CacheBuilder.newBuilder()
      .concurrencyLevel(concurrencyLevel)
      .recordStats
      .build[Object, Object]
  else
    CacheBuilder.newBuilder()
      .concurrencyLevel(concurrencyLevel)
      .expireAfterWrite(timeoutMinutes.toLong, TimeUnit.MINUTES)
      .recordStats
      .build[Object, Object]
}

object StrongCache {
  @inline def apply[Key<:Any, Value<:Any](concurrencyLevel: Int=4, timeoutMinutes: Int=0)
                                         (implicit executionContext: CacheExecutionContext): StrongCache[Key, Value] =
    new StrongCache[Key, Value](concurrencyLevel, timeoutMinutes){}
}


/** Features strong values that might expire.
  * @param timeoutMinutes if 0 (minutes), cache entries never expire; otherwise specifies that each entry should be automatically
  * removed from the cache once a fixed duration has elapsed after the entry's creation, the most recent replacement of its value,
  * or its last access. Access time is reset by all cache read and write operations.
  * @param concurrencyLevel Guides the allowed concurrency among update operations. Used as a hint for internal sizing. The
  * table is internally partitioned to try to permit the indicated number of concurrent updates
  * without contention. Because assignment of entries to these partitions is not necessarily
  * uniform, the actual concurrency observed may vary. Ideally, you should choose a value to
  * accommodate as many threads as will ever concurrently modify the table. Using a significantly
  * higher value than you need can waste space and time, and a significantly lower value can lead
  * to thread contention. But overestimates and underestimates within an order of magnitude do not
  * usually have much noticeable impact. A value of one permits only one thread to modify the cache
  * at a time, but since read operations and cache loading computations can proceed concurrently,
  * this still yields higher concurrency than full synchronization.
  * <p> Defaults to 4. <b>Note:</b>The default may change in the future. If you care about this
  * value, you should always choose it explicitly.
  * <p>The current implementation uses the concurrency level to create a fixed number of hashtable
  * segments, each governed by its own write lock. The segment lock is taken once for each explicit
  * write, and twice for each cache loading computation (once prior to loading the new value,
  * and once after loading completes). Much internal cache management is performed at the segment
  * granularity. For example, access queues and write queues are kept per segment when they are
  * required by the selected eviction algorithm. As such, when writing unit tests it is not
  * uncommon to specify `concurrencyLevel(1)` in order to achieve more deterministic eviction behavior. */
abstract class AbstractCache[Key<:Any, Value<:Any](val concurrencyLevel: Int=4, val timeoutMinutes: Int=5)
                                                  (implicit val executionContext: CacheExecutionContext) {
  /** The underlying Google Guava `Cache` instance */
  def underlying: Cache[Object, Object]

  /** Returns `Some(`value associated with `key` in this cache`)`, or None if there is no cached value for `key`. */
  @inline def get(key: Key): Option[Value] = Option(underlying.getIfPresent(key).asInstanceOf[Value])

  @inline def getAll: List[Value] = underlying.asMap.values.toArray.toList.asInstanceOf[List[Value]]

  /** Returns the value associated with `key` in this cache, obtaining that value from
   * `defaultValue` if necessary. No observable state associated with this cache is modified
   * until loading completes. This method provides a simple substitute for the conventional
   * "if cached, return; otherwise create, cache and return" pattern.
   * <p><b>Warning:</b> `defaultValue` <b>must not</b> evaluate to `null`. */
  @inline def getWithDefault(key: Key, defaultValue: => Value): Value =
    underlying.get(key.asInstanceOf[Object],
      new Callable[Object] {
        override def call: Object = defaultValue.asInstanceOf[Object]
      }
    ).asInstanceOf[Value]

  /** Like `getWithDefault`, but useful when `defaultValue` is expensive to compute */
  @inline def getWithDefaultAsync(key: Key, defaultValue: => Value): Future[Value] =
    Future { getWithDefault(key, defaultValue) }

  /** Associates `value` with `key` in this cache. If the cache previously contained a
   * value associated with `key`, the old value is atomically replaced by `value`. */
  @inline def put(key: Key, value: Value): AbstractCache[Key, Value] = {
    underlying.put(key.asInstanceOf[Object], value.asInstanceOf[Object])
    this
  }

  /** Like `put`, but useful when `value` is expensive to compute */
  @inline def putAsync(key: Key, value: => Value): Future[AbstractCache[Key, Value]] =
    Future {
      underlying.put(key.asInstanceOf[Object], value.asInstanceOf[Object])
      this
    }

  /** Like `put`, but also returns `value` */
  @inline def putGet(key: Key, value: Value): AbstractCache[Key, Value] = {
    underlying.put(key.asInstanceOf[Object], value.asInstanceOf[Object])
    this
  }

  /** Like `putGet`, but useful when `value` is expensive to compute */
  @inline def putGetAsync(key: Key, value: => Value): Future[AbstractCache[Key, Value]] = Future {
    underlying.put(key.asInstanceOf[Object], value.asInstanceOf[Object])
    this
  }

  /** Removes `key` from the underlying cache */
  @inline def remove(key: Key): AbstractCache[Key, Value] = {
    underlying.invalidate(key)
    this
  }
}
