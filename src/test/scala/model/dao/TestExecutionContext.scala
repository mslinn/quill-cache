package model.dao

import model.persistence.CacheExecutionContext
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/** Just delegates to standard Scala ExecutionContext, you can make this do whatever you want */
object TestExecutionContext extends CacheExecutionContext {
  protected val ec: ExecutionContext = ExecutionContext.Implicits.global
  override def execute(runnable: Runnable): Unit = ec.execute(runnable)

  override def reportFailure(cause: Throwable): Unit = ec.reportFailure(cause)
}
