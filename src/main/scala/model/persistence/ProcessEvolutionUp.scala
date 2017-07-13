package model.persistence

import org.slf4j.Logger
import scala.concurrent.ExecutionContext
import scala.io.Codec
import scala.io.Source.fromInputStream

class Blah

/** Extract the Up portion of a Play evolution file and execute SQL statements, including DDL */
object ProcessEvolutionUp {
  val Logger: Logger = org.slf4j.LoggerFactory.getLogger("persistence")

  /** @param target must be lower case */
  protected def contains(line: String, target: String): Boolean =
    line.toLowerCase.replaceAll("\\s+", " ") contains target

  def getLines(classLoader: ClassLoader, resource: String): Option[(String, List[String])] =
    try {
      val stream = classLoader.getResourceAsStream(resource)
      val content = fromInputStream(stream)(Codec.UTF8)
      val lines: Iterator[String] = content.getLines
      Some(resource -> lines.toList)
    } catch {
      case e: Throwable =>
        Logger.warn(e.toString)
        None
    }

  /** Tries to load the resource 3 ways */
  protected def fromResource(resourcePath: String, fallbackPath: String)
                            (implicit codec: Codec): (String, List[String]) = {
    getLines(classOf[Blah].getClassLoader, resourcePath)
      .orElse(getLines(Thread.currentThread.getContextClassLoader, resourcePath))
      .getOrElse(fallbackPath -> scala.io.Source.fromFile(fallbackPath).getLines.toList)
  }

  protected def ups(resourcePath: String, fallbackPath: String): String = {
    val (source, allSql) = fromResource(resourcePath, fallbackPath)
    val upsLines: Seq[String] = allSql
      .dropWhile(!contains(_, "# --- !Ups".toLowerCase))
      .drop(1)
      .takeWhile(!contains(_, "# --- !Downs".toLowerCase))
    val ups = upsLines.mkString("\n")
    Logger.warn(s"Got ${ upsLines.length } lines from $source:]\n$ups")
    ups
  }

  /** Works with synchronous Quill contexts */
  def apply(selectedCtx: CtxLike, resourcePath: String, fallbackPath: String): Unit = {
    selectedCtx.ctx.executeAction(ups(resourcePath, fallbackPath))
    ()
  }

  /** Works with asynchronous Quill contexts.
    * Requires an implicit [[ExecutionContext]], uses `concurrent.ExecutionContext.Implicits.global` if none found. */
  def apply(selectedCtx: AsyncCtxLike, resourcePath: String, fallbackPath: String)
           (implicit ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global): Unit = {
    selectedCtx.ctx.executeAction(ups(resourcePath, fallbackPath))
    ()
  }
}
