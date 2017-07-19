package model

import model.dao.SelectedCtx
import persistence.QuillCacheImplicits

case object Ctx extends SelectedCtx with QuillCacheImplicits
