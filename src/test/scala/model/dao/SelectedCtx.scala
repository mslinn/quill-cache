package model.dao

/** Define `SelectedCtx` for use with all DAOs */
trait SelectedCtx extends model.persistence.H2Ctx
object SelectedCtx extends SelectedCtx
