package model.dao

import model.User
import model.persistence.Types.IdOptionLong
import model.persistence._

object UserDAO extends CachedPersistence[Long, Option[Long], User]
    with StrongCacheLike[Long, Option[Long], User] {
  import model.dao.Ctx._

  @inline def _findAll: List[User] = run { quote { query[User] } }

  @inline def deleteAll(): Unit = _findAll.foreach(user => deleteById(user.id))

  val queryById: IdOptionLong => Quoted[EntityQuery[User]] =
    (id: IdOptionLong) =>
      quote { query[User].filter(_.id == lift(id)) }

  val _deleteById: (IdOptionLong) => Unit =
    (id: IdOptionLong) => {
      run { quote { queryById(id).delete } }
      ()
    }

  val _findById: IdOptionLong => Option[User] =
    (id: Id[Option[Long]]) =>
      run { quote { queryById(id) } }.headOption

  val _insert: User => User =
    (user: User) => {
      val id: Id[Option[Long]] = try {
        run { quote { query[User].insert(lift(user)) }.returningGenerated(_.id) }
      } catch {
        case e: Throwable =>
          logger.error(e.getMessage)
          throw e
      }
      user.setId(id)
    }

  val _update: User => User =
    (user: User) => {
      run { queryById(user.id).update(lift(user)) }
      user
    }

  @inline override def findById(id: IdOptionLong): Option[User] =
    id.value.map(theCache.get).getOrElse { run { queryById(id) }.headOption }

  @inline def findByUserId(userId: String): Option[User] =
    findAll.find(_.userId==userId).orElse {
      run { query[User].filter(_.userId == lift(userId)) }.headOption
    }
}
