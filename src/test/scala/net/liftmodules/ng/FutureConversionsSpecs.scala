package net.liftmodules.ng

import org.scalatest.{Matchers, WordSpec}
import net.liftweb.common._
import net.liftweb.actor.LAFuture
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class FutureConversionsSpecs extends WordSpec with Matchers with ScalaFutures {
  "The Scala Future => LAFuture converter" should {
    import FutureConversions._

    "convert a successful Future[String] to a satisfied LAFuture(Full[String])" in {
      val sf  = Future("scala")
      val laf: LAFuture[Box[String]] = sf

      laf.get(3000) shouldBe Full("scala")
    }

    "convert a failed Future[String] to a satisfied LAFuture(Failure[String])" in {
      val ex = new Exception("the future failed")
      val sf:  Future[String] = Future.failed(ex)
      val laf: LAFuture[Box[String]] = sf

      laf.get(3000) shouldBe Failure("the future failed", Full(ex), Empty)
    }

    "clean up invalid json-characters present in an Exception message" in {
      val ex = new Exception(
        """the future failed
          |quite badly""".stripMargin
      )
      val sf:  Future[String] = Future.failed(ex)
      val laf: LAFuture[Box[String]] = sf

      laf.get(3000) shouldBe Failure("the future failed\\u000aquite badly", Full(ex), Empty)
    }
  }

  "The Scala Future.boxed converter" should {
    import FutureConversions._

    "convert a successful Future[String] into a Future[Full[String]]" in {
      val f:  Future[String] = Future("scala")
      val fb: Future[Box[String]] = f.boxed

      whenReady(fb)( _ shouldEqual Full("scala") )
    }

    "convert a successful Future(null) into Future(Empty)" in {
      val f:  Future[String] = Future(null)
      val fb: Future[Box[String]] = f.boxed

      whenReady(fb)( _ shouldEqual Empty )
    }

    "convert a failed Future[_] into a Future(Failure)" in {
      val e = new Exception("test")
      val f:  Future[String] = Future.failed(e)
      val fb: Future[Box[String]] = f.boxed

      whenReady(fb)( _ shouldEqual Failure("test", Full(e), Empty) )
    }

    "not double box a Future[Box[T]]" in {
      val f:  Future[Box[String]] = Future(Full("scala"))
      val fb: Future[Box[String]] = f.boxed

      whenReady(fb)( _ shouldEqual Full("scala") )
    }

    "preserve a Future[Failure]" in {
      val e = new Exception("test")
      val failure = Failure("test", Full(e), Empty)
      val f:  Future[Box[String]] = Future(failure)
      val fb: Future[Box[String]] = f.boxed

      whenReady(fb)( _ shouldEqual failure )
    }
  }

  "The LAFuture => Scala Future converter" should {
    import FutureConversions._

    def laf[T](v: Box[T]): LAFuture[Box[T]] = {
      val f = new LAFuture[Box[T]]()
      f.satisfy(v)
      f
    }

    "convert LAFuture[Full[String]] to a satisfied Future[String]" in {
      val l = laf(Full("scala"))
      val s: Future[String] = l.asScala

      whenReady(s){ _ shouldBe "scala" }
    }

    "convert a LAFuture[Failure] to failed Future[String], forwarding the exception" in {
      val ex = new Exception("the future failed")
      val l = laf[String](Failure("the future failed", Full(ex), Empty))
      val s:  Future[Throwable] = l.asScala.failed

      whenReady(s){ _ shouldBe ex }
    }

    "convert an LAFuture[Failure] to a failed Future[String], forwarding the Failure message" in {
      val l = laf[String](Failure("the future failed"))
      val s: Future[Throwable] = l.asScala.failed

      whenReady(s){ _.getMessage shouldBe "the future failed" }
    }
  }
}
