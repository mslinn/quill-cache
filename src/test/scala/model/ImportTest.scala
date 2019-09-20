package model

import org.scalatest.{Matchers, WordSpec}

class Klass {
  implicit val int: Int = 15
  implicit val double: Double = 30.0
}

class Container(val klass: Klass) {
  import klass._

  def gimmeInt: Int = implicitly[Int]
}

case object CaseObject {
  implicit val int: Int = 15
  implicit val double: Double = 30.0
}

case object ContainerSingleton extends Container(new Klass)

class ImportTest extends WordSpec with Matchers {
  "Scala imports" should {
    "be provided by a class" in {
      val klass = new Klass
      import klass._
      implicitly[Int] shouldBe klass.int
      implicitly[Double] shouldBe klass.double
    }

    "be provided by a class object" in {
      import model.CaseObject._
      implicitly[Int] shouldBe CaseObject.int
      implicitly[Double] shouldBe CaseObject.double
    }

    "be available inside a class" in {
      val klass = new Klass
      val container = new Container(klass)
      container.gimmeInt shouldBe klass.int
    }

    "be available inside a singleton" in {
      import model.ContainerSingleton.klass._
      implicitly[Int] shouldBe ContainerSingleton.gimmeInt
    }
  }
}
