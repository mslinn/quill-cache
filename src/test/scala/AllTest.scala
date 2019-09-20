import model.dao.PersistenceTest
import model.{ContextTest, FindAllTest, ImportTest, QuillConfigTest}
import org.scalatest.Sequential

class AllTest extends Sequential(
  new QuillConfigTest,
  new ContextTest,
  new FindAllTest,
  new PersistenceTest,
  new ImportTest
)
