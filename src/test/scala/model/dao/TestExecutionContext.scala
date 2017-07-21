package model.dao

import scala.concurrent.ExecutionContext

object TestExecutionContext {
  // Define any execution context you desire; here we merely use the Scala default
  implicit lazy val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global
}
