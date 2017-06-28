package model.persistence

import scala.reflect.macros.whitebox.{Context => MacroContext}

/** Sample macro */
class InsertOrUpdateMacro(val c: MacroContext) {
  import c.universe._

  def insertOrUpdate[T](entity: Tree, filter: Tree)
                       (implicit t: WeakTypeTag[T]): Tree =
    q"""
      import ${c.prefix}._
      if (run(${c.prefix}.quote {
        ${c.prefix}.query[$t].filter($filter).update(lift($entity))
      }) == 0) {
          run(quote {
            query[$t].insert(lift($entity))
          })
      }
      ()
    """
}
