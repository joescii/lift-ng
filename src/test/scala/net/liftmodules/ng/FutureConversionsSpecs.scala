package net.liftmodules.ng

import org.scalatest.{ WordSpec, Matchers }
import net.liftweb.common._
import net.liftweb.actor.LAFuture
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global


class FutureConversionsSpecs extends WordSpec with Matchers {
  "The Scala Future => LAFuture converter" should {
    import FutureConversions._

    "convert a successful Future[String] to a satisfied LAFuture(Full[String])" in {
      val sf  = Future("scala")
      val laf:LAFuture[Box[String]] = sf

      laf.get(3000) should be (Full("scala"))
    }

    "convert a failed Future[String] to a satisfied LAFuture(Failure[String])" in {
      val ex = new Exception("the future failed")
      val sf:Future[String] = Future.failed(ex)
      val laf:LAFuture[Box[String]] = sf

      laf.get(3000) should be (Failure("the future failed", Full(ex), Empty))
    }

    "clean up invalid json-characters present in an Exception message" in {
      val ex = new Exception(
        """the future failed
          |quite badly""".stripMargin
      )
      val sf:Future[String] = Future.failed(ex)
      val laf:LAFuture[Box[String]] = sf

      laf.get(3000) should be (Failure("the future failed\\u000aquite badly", Full(ex), Empty))
    }
  }
}
