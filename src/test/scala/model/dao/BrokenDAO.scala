package model.dao

import model.Token
import model.persistence.Types.IdOptionLong
import model.persistence._

object BrokenDAO extends UnCachedPersistence[Long, Option[Long], Token] {
  import model.dao.Ctx._

  @inline def _findAll: List[Token] = run { quote { query[Token] } }

  val queryById: IdOptionLong => Ctx.Quoted[Ctx.EntityQuery[Token]] =
    (id: IdOptionLong) =>
      quote { query[Token].filter(_.id == lift(id)) }

  val _deleteById: IdOptionLong => Unit =
    (id: IdOptionLong) => {
      run { quote { queryById(id).delete } }
      ()
    }

  @inline def deleteAll(): Unit = _findAll.foreach(x => deleteById(x.id))

  val _findById: Id[Option[Long]] => Option[Token] =
    (id: Id[Option[Long]]) =>
      run { quote { queryById(id) } }.headOption

  val _insert: Token => Token =
    (token: Token) => {
      val id: Id[Option[Long]] = try {
        run { quote { query[Token].insert(lift(token)) }.returningGenerated(_.id) }
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

  @inline override def findById(id: IdOptionLong): Option[Token] =
    run { queryById(id) }.headOption
}
