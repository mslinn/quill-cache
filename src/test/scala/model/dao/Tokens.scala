package model.dao

import model.Token
import model.persistence.Types.IdOptionLong
import model.persistence._

object Tokens extends UnCachedPersistence[Long, Option[Long], Token]
     with QuillImplicits
     with SelectedCtx {
  import ctx._

  @inline def _findAll: List[Token] = run { quote { query[Token] } }

  val queryById: (IdOptionLong) => Tokens.ctx.Quoted[Tokens.ctx.EntityQuery[Token]] =
    (id: IdOptionLong) =>
      quote { query[Token].filter(_.id == lift(id)) }

  val _deleteById: (IdOptionLong) => Unit =
    (id: IdOptionLong) => {
      run { quote { queryById(id).delete } }
      ()
    }

  val _findById: (Id[Option[Long]]) => Option[Token] =
    (id: Id[Option[Long]]) =>
      run { quote { queryById(id) } }.headOption

  val _insert: Token => Token =
    (token: Token) => {
      val id: Id[Option[Long]] = try {
        run { quote { query[Token].insert(lift(token)) }.returning(_.id) }
      } catch {
        case e: Throwable =>
          logger.error(e.getMessage)
          throw e
      }
      token.setId(id)
    }

  val _update: Token => Token =
    (token: Token) => { // TODO is there any way to write this using autoincrement so `_findById` does not need to be called?
      run { queryById(token.id).update(lift(token)) }
      _findById(token.id).get
    }

  val className = "Token"

  @inline override def findById(id: IdOptionLong): Option[Token] =
    run { queryById(id) }.headOption
}
