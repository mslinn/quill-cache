package model.dao

import model.User
import model.persistence.Types.IdOptionLong
import model.persistence.{CachedPersistence, H2Ctx, Id, QuillImplicits, SoftCacheLike}
import scala.concurrent.ExecutionContext

/** Define `SelectedCtx` for use with all DAOs */
trait SelectedCtx extends H2Ctx


object Users extends CachedPersistence[Long, Option[Long], User]
             with SoftCacheLike[Long, Option[Long], User]
             with QuillImplicits
             with SelectedCtx {
  import ctx._

  /** A real application would provide a dedicated `ExecutionContext` for DAOs */
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override val _findAll: List[User] =
    run { quote { query[User] } }

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
          Logger.error(e.getMessage)
          throw e
      }
      user.setId(id)
    }

  val _update: User => User =
    (user: User) => {
      val id: Long = run { queryById(user.id).update(lift(user)) }
      user.setId(Id(Some(id)))
    }

  val className = "User"

  @inline override def findById(id: IdOptionLong): Option[User] =
    id.value.map(theCache.get).getOrElse { run { queryById(id) }.headOption }

  @inline def findByUserId(userId: String): Option[User] =
    findAll.find(_.userId==userId).orElse {
      run { query[User].filter(_.userId == lift(userId)) }.headOption
    }

  @inline def create(email: String, userId: String, password: String, firstName: String, lastName: String): (String, String) = {
    if (findByUserId(userId).isDefined) {
      "error" -> s"UserID $userId is already in use."
    } else {
      run { quote {
        query[User]
          .insert(lift(User(email=email, firstName=firstName, lastName=lastName, userId=userId, password=password)))
          .returning(_.id.value)
      } }
      "success" -> s"Created user $userId"
    }
  }
}
