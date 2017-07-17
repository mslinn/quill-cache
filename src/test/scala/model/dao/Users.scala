package model.dao

import model.User
import model.persistence.Types.IdOptionLong
import model.persistence._

object Users extends CachedPersistence[Long, Option[Long], User]
     with StrongCacheLike[Long, Option[Long], User]
     with QuillImplicits
     with SelectedCtx {
  import ctx._

  @inline def _findAll: List[User] = run { quote { query[User] } }

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
        run { quote { query[User].insert(lift(user)) }.returning(_.id) }
      } catch {
        case e: Throwable =>
          logger.error(e.getMessage)
          throw e
      }
      user.setId(id)
    }

  val _update: User => User =
    (user: User) => { // TODO is there any way to write this using autoincrement so `_findById` does not need to be called?
      run { queryById(user.id).update(lift(user)) }
      _findById(user.id).get
    }

  val className = "User"

  @inline override def findById(id: IdOptionLong): Option[User] =
    id.value.map(theCache.get).getOrElse { run { queryById(id) }.headOption }

  @inline def findByUserId(userId: String): Option[User] =
    findAll.find(_.userId==userId).orElse {
      run { query[User].filter(_.userId == lift(userId)) }.headOption
    }
}
