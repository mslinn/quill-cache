package model.persistence

import com.google.inject.{AbstractModule, Provider}
import net.codingwell.scalaguice.ScalaModule
import org.slf4j.Logger
import scala.concurrent.ExecutionContext

class DefaultExecutionContextProvider extends Provider[ExecutionContext] {
  def get: ExecutionContext = scala.concurrent.ExecutionContext.global
}

class DBModule extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    bind[ExecutionContext].toProvider[DefaultExecutionContextProvider].asEagerSingleton
  }
}

object DBComponent {
  import scala.concurrent.duration._
  import scala.language.postfixOps

  val Logger: Logger = org.slf4j.LoggerFactory.getLogger("persistence")
  val dbDuration: FiniteDuration = 1 minute
}

trait DBComponent {
  implicit val ec: ExecutionContext
}
