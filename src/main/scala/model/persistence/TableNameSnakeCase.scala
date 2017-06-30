package model.persistence

import ai.x.safe._
import io.getquill.{Escape, NamingStrategy, SnakeCase}

/** Ensures that table names are quoted and snake_case but never start with a leading _. */
trait TableNameSnakeCase extends NamingStrategy with Escape with SnakeCase {
  override def table(s: String): String   = {
    val x = super.default(s)
    val y = if (x.startsWith("_")) x.substring(1) else x
    safe""""$y""""
  }
}

object TableNameSnakeCase extends TableNameSnakeCase
