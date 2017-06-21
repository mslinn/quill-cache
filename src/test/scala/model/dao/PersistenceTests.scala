package model.dao

import model.Course
import model.persistence.{Id, Quill}
import org.scalatest._

class PersistenceTests extends WordSpec with Matchers with Quill {
  val course: Course = Courses.upsert(Course(groupId=Id(Some(99)), sku=s"course_Blah"))

  // Ensure connection pool works
  (1L to 299L).foreach { i =>
    Courses.upsert(Course(groupId=Id(Some(i)), sku=s"course_$i"))
  }

  "Course instances" must  {
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
