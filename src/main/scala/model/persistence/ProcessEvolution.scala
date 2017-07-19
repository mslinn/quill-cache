package model.persistence

import io.getquill.context.async.AsyncContext
import io.getquill.context.jdbc.JdbcContext
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.{Implicits => EC}
import scala.io.Codec
import scala.io.Source.fromInputStream

class Blah

/** Extract the Up portion of a Play evolution file and execute SQL statements, including DDL */
class ProcessEvolution(resourcePath: String, fallbackPath: String) {
  /** @param target must be lower case */
  protected def contains(line: String, target: String): Boolean =
    line.toLowerCase.replaceAll("\\s+", " ") contains target

  protected def getLines(classLoader: ClassLoader, resource: String): Option[(String, List[String])] =
    try {
      val stream = classLoader.getResourceAsStream(resource)
      val content = fromInputStream(stream)(Codec.UTF8)
      val lines: Iterator[String] = content.getLines
      Some(resource -> lines.toList)
    } catch {
      case e: Throwable =>
        logger.warn(e.toString)
        None
    }

  /** Tries to load the resource 3 ways */
  protected def fromResource(resourcePath: String, fallbackPath: String)
                            (implicit codec: Codec): (String, List[String]) = {
    getLines(classOf[Blah].getClassLoader, resourcePath)
      .orElse(getLines(Thread.currentThread.getContextClassLoader, resourcePath))
      .getOrElse(fallbackPath -> scala.io.Source.fromFile(fallbackPath).getLines.toList)
  }

  protected def downs(resourcePath: String, fallbackPath: String): String = {
    val (source, allSql) = fromResource(resourcePath, fallbackPath)
    val downsLines: Seq[String] = allSql
      .dropWhile(!contains(_, "# --- !Downs".toLowerCase))
      .drop(1)
    val downs = downsLines.mkString("\n")
    logger.warn(s"Got ${ downsLines.length } down lines from $source:]\n$downs")
    downs
  }

  protected def ups(resourcePath: String, fallbackPath: String): String = {
    val (source, allSql) = fromResource(resourcePath, fallbackPath)
    val upsLines: Seq[String] = allSql
      .dropWhile(!contains(_, "# --- !Ups".toLowerCase))
      .drop(1)
      .takeWhile(!contains(_, "# --- !Downs".toLowerCase))
    val ups = upsLines.mkString("\n")
    logger.warn(s"Got ${ upsLines.length } up lines from $source:]\n$ups")
    ups
  }

  /** Works with synchronous Quill contexts */
  def ups(ctx: JdbcContext[_, _]): Unit = {
    ctx.executeAction(ups(resourcePath, fallbackPath))
    ()
  }

  /** Works with synchronous Quill contexts */
  def downs(ctx: JdbcContext[_, _]): Unit = {
    ctx.executeAction(downs(resourcePath, fallbackPath))
    ()
  }

  /** Works with asynchronous Quill contexts.
    * Looks for an implicit [[concurrent.ExecutionContext]], uses [[concurrent.ExecutionContext.Implicits.global]] if none found. */
  def ups(ctx: AsyncContext[_, _, _])
         (implicit ec: ExecutionContext = EC.global): Unit = {
      ctx.executeAction(ups(resourcePath, fallbackPath))
    ()
  }

  /** Works with asynchronous Quill contexts.
    * Looks for an implicit [[concurrent.ExecutionContext]], uses [[concurrent.ExecutionContext.Implicits.global]] if none found. */
  def downs(ctx: AsyncContext[_, _, _])
           (implicit ec: ExecutionContext = EC.global): Unit = {
      ctx.executeAction(downs(resourcePath, fallbackPath))
    ()
  }
}
