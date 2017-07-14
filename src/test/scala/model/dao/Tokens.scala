package model.dao

import model.Token
import model.persistence._
import model.persistence.Types.IdOptionLong
import scala.concurrent.ExecutionContext

object Tokens extends UnCachedPersistence[Long, Option[Long], Token]
             with QuillImplicits
             with SelectedCtx {
  import ctx._

  /** A real application would provide a dedicated `ExecutionContext` for DAOs */
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override val _findAll: List[Token] = run { quote { query[Token] } }

  val queryById: IdOptionLong => Quoted[EntityQuery[Token]] =
    (id: IdOptionLong) =>
      quote { query[Token].filter(_.id == lift(id)) }

  val _deleteById: (IdOptionLong) => Unit =
    (id: IdOptionLong) => {
      run { quote { queryById(id).delete } }
      ()
    }

  val _findById: IdOptionLong => Option[Token] =
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
    (token: Token) => {
      val id: Long = run { queryById(token.id).update(lift(token)) }
      token.setId(Id(Some(id)))
    }

  val className = "Token"

  @inline override def findById(id: IdOptionLong): Option[Token] =
    { run { queryById(id) }.headOption }
}
