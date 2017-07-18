import model.{FindAllTest, QuillConfigTest}
import model.dao.PersistenceTest
import org.scalatest.Suites

class AllTest extends Suites(
  new QuillConfigTest,
  new FindAllTest,
  new PersistenceTest
)
