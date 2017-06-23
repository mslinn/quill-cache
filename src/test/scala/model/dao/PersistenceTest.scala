package model.dao

import model.Course
import model.persistence.{Copier, Id, QuillConfiguration}
import org.scalatest._

case class X(a: String, id: Int)

class PersistenceTest extends WordSpec with Matchers with QuillConfiguration {
  val course: Course = Courses.upsert(Course(groupId=Id(Some(99)), sku=s"course_Blah"))

  // Ensure connection pool works
  (1L to 299L).foreach { i =>
    Courses.upsert(Course(groupId=Id(Some(i)), sku=s"course_$i"))
  }

  "Copier" should {
    "work" in {
        val x = X("hi", 123)
        val result = Copier(x, ("id", 456))
        result shouldBe X("hi", 456)
    }
  }

  "Course instances" should  {
    "be found by sku" in {
      (1 to 99).foreach { i =>
        Courses.findBySku(s"course_$i").size shouldBe 1
      }
    }

    "be found by id" in {
      Courses.findById(course.id).size shouldBe 1
    }
  }
}
