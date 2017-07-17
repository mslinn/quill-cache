import model.FindAllTest
import model.dao.PersistenceTest
import org.scalatest.Suites

class AllTest extends Suites(
  new FindAllTest,
  new PersistenceTest
)
