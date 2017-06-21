import model.dao.Courses
import org.scalatest._

class CompilationTest extends WordSpec with MustMatchers { this: Suite =>
  "Blah" must {
     "blah" in {
       println("Compiled! Hurray!")
     }
   }
}

trait TestSetup extends WordSpec
    with BeforeAndAfterAll
    with BeforeAndAfter

trait Lifecycle { this: TestSetup =>
  def wipeDB(): Unit = Courses.findAll.foreach(c => Courses.deleteById(c.id))

  override def afterAll(): Unit = wipeDB()

  override def beforeAll(): Unit = { }

  wipeDB() // just making sure; if the previous test aborted then we need to clean up
}
