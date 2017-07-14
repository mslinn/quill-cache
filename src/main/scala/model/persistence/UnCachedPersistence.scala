package model.persistence

import io.getquill._

/** Accesses the table for each query.
  * You can use this abstract class to derive DAOs for case classes that must have direct access to the database so the
  * case classes are not cached. You don't have to subclass `UnCachedPersistence`, but if you do then the DAOs for your
  * cached domain objects will have the same interface as the DAOs for your uncached domain objects. */
abstract class UnCachedPersistence[Key <: Any, _IdType <: Option[Key], CaseClass <: HasId[CaseClass, _IdType]]
  extends QuillImplicits with IdImplicitLike with CtxLike {
  /** Encapsulates the Quill query that returns all instances of the case class from the database */
  def _findAll: List[CaseClass]

  /** Encapsulates the Quill query that deletes the instance of the case class with the given `Id` from the database */
  def _deleteById: (Id[_IdType]) => Unit

  /** Encapsulates the Quill query that optionally returns the instance of the case class from the database with the given
    * `Id`, or `None` if not found. */
  def _findById: Id[_IdType] => Option[CaseClass]

  /** Encapsulates the Quill query that inserts the given instance of the case class into the database, and returns the
    * case class as it was stored, including any auto-increment fields. */
  def _insert: CaseClass => CaseClass

  /** Encapsulates the Quill query that updates the given instance of the case class into the database, and returns the entity.
    * Throws an Exception if the case class was not previously persisted. */
  def _update: CaseClass => CaseClass

  /** Human-readable name of persisted class */
  def className: String

  lazy val tableName: String = TableNameSnakeCase.table(className) // todo publish this new version

  @inline def delete(caseClass: CaseClass): Unit = {
    logger.debug(s"Deleting $className #${ caseClass.id } from database and cache")
    deleteById(caseClass.id)
    ()
  }

  @inline def deleteById(id: Id[_IdType]): Unit = {
    logger.debug(s"Deleting $className #$id from database and cache")
    _deleteById(id)
    ()
  }

  @inline def findAll: List[CaseClass] = {
    logger.debug(s"Fetching all ${ className }s from database")
    try { _findAll } catch {
      case ex: Exception =>
        logger.error(ex.format())
        Nil
    }
  }

  /** Always fetches from the database; bypass cache, useful for startup code. See also [[CachedPersistence.findAll]] */
  @inline def findAllFromDB: List[CaseClass] = {
    try {
      val caseClasses: List[CaseClass] = _findAll
      logger.trace(s"Fetched all ${ caseClasses.size } ${ className }s from database")
      caseClasses
    } catch {
      case ex: Exception =>
        logger.error(s"_findAll $className " + ex.format())
        Nil
    }
  }

  /** Always fetches from the database; bypass cache, useful for startup code. See also `findById` */
  @inline def findByIdFromDB(id: Id[_IdType]): Option[CaseClass] = _findById(id)

  @inline def findById(id: Id[_IdType]): Option[CaseClass] = _findById(id)

  @inline def getId(t: CaseClass): Id[Key] = t.getClass.getDeclaredMethods
    .find(_.getName=="id")
    .get
    .invoke(t)
    .asInstanceOf[Id[Key]]

  /** Sanitizes and inserts `caseClass`.
    * Does not check to see if `caseClass` was previously persisted */
  @inline def insert(caseClass: CaseClass): CaseClass = _insert(sanitize(caseClass))

  /** Searches by `CaseClass.id`
   * @return true if `CaseClass` was removed (false if the `CaseClass` was not defined prior) */
  @inline def remove(t: CaseClass): Unit = t.id.value match {
    case _: Some[_IdType] => deleteById(t.id)
    case x if x==None => ()
  }

  /** This method only has relevance for Postgres databases; it ensures that the next `autoInc` value is properly set when the app starts.
    * Assumes that column named `id` is present, and that `$${ tableName }_id_seq` exists.
    * There is no harm in invoking this method of other types of databases,
    * because it does not do anything unless the database is Postgres.
    * @see [[https://stackoverflow.com/a/244265/553865]]
    * List sequences with {{{\ds}}} */
  @inline def setAutoInc(): Unit = if (ctx.isInstanceOf[PostgresJdbcContext[_]]) try {
    val selectStmt = s"SELECT max(id) FROM $tableName"
    logger.info(s"About to find highest index for $tableName with '$selectStmt'")
    val maxId: Long = ctx.executeQuerySingle(selectStmt, extractor = _.getLong(1))

    val seqName = tableName.replaceAll(""""$""", s"""_id_seq"""")
    val alterSeq = s"ALTER SEQUENCE $seqName RESTART WITH ${ maxId + 1L }"
    logger.info(s"About to set autoinc for $tableName with '$alterSeq'")
    ctx.executeAction(alterSeq)
    ()
  } catch {
    case ex: Throwable =>
      logger.error(ex.format())
  }

  /** Sanitizes and updates `caseClass`.
    * Does not check to see if `caseClass` was previously persisted */
  @inline def update(caseClass: CaseClass): CaseClass = _update(sanitize(caseClass))

  /** Sanitizes and inserts or updates `caseClass` */
  @inline def upsert(caseClass: CaseClass): CaseClass =
    try {
      val notFound = caseClass.id.value.isEmpty || findById(caseClass.id).isEmpty
      val result: CaseClass = if (notFound) insert(caseClass) else update(caseClass)
      result
    } catch {
      case ex: Exception =>
        logger.error(s"upsert $caseClass" + ex.format())
        throw ex
    }

  // Empty out the backing table; normally just used for testing
  @inline def zap(): Unit = _findAll.foreach(remove)

  protected val sanitize: CaseClass => CaseClass =
    (t: CaseClass) => t

  protected val copyUpdate: (CaseClass, Id[_IdType]) => CaseClass =
    (t: CaseClass, id: Id[_IdType]) => sanitize(t.setId(id))
}
