package model.persistence

import ai.x.safe._
import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Codec}
import scala.io.Source.fromInputStream

/** Extract the Up portion of a Play evolution file and execute SQL statements, including DDL */
object ProcessEvolutionUp {
  /** @param target must be lower case */
  protected def contains(line: String, target: String): Boolean =
    line.toLowerCase.replaceAll("\\s+", " ").safeContains(target)

  protected def fromResource(resource: String, classLoader: ClassLoader = Thread.currentThread.getContextClassLoader)
                  (implicit codec: Codec): BufferedSource =
    fromInputStream(classLoader.getResourceAsStream(resource))

  protected def ups(resource: String): String =
    fromResource(resource).getLines
      .dropWhile(!safeContains(_, "# --- !Ups".toLowerCase))
      .drop(1)
      .takeWhile(!safeContains(_, "# --- !Downs".toLowerCase))
      .safeMkString("\n")

  /** Works with synchronous Quill contexts */
  def apply(selectedCtx: CtxLike, resource: String): Unit = {
    selectedCtx.ctx.executeAction(ups(resource))
    ()
  }

  /** Works with asynchronous Quill contexts.
    * Requires an implicit [[ExecutionContext]], uses `concurrent.ExecutionContext.Implicits.global` if none found. */
  def apply(selectedCtx: AsyncCtxLike, resource: String)
           (implicit ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global): Unit = {
    selectedCtx.ctx.executeAction(ups(resource))
    ()
  }
}
