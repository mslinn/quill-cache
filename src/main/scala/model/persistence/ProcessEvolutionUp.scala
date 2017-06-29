package model.persistence

import scala.io.{BufferedSource, Codec}
import scala.io.Source.fromInputStream

/** Extract the Up portion of Play Evolution file and execute those SQL statements */
object ProcessEvolutionUp {
  import H2Configuration.ctx

  /** @param target must be lower case */
  protected def contains(line: String, target: String): Boolean =
    line.toLowerCase.replaceAll("\\s+", " ") contains target

  def fromResource(resource: String, classLoader: ClassLoader = Thread.currentThread.getContextClassLoader)
                  (implicit codec: Codec): BufferedSource =
    fromInputStream(classLoader.getResourceAsStream(resource))

  def apply(resource: String): Unit = {
    val upString: String = fromResource(resource).getLines
      .dropWhile(!contains(_, "# --- !Ups".toLowerCase))
      .drop(1)
      .takeWhile(!contains(_, "# --- !Downs".toLowerCase))
      .mkString("\n")
    ctx.executeAction(upString)
    ()
  }
}
