import model.{ContextTest, FindAllTest, ImportTest, QuillConfigTest}
import model.dao.PersistenceTest
import org.scalatest.Suites

class AllTest extends Suites(
  new QuillConfigTest,
  new ContextTest,
  new FindAllTest,
  new PersistenceTest,
  new ImportTest
)
