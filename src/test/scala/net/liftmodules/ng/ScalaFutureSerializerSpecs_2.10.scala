package net.liftmodules.ng

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import net.liftweb.json.{JsonParser, NoTypeHints, Serialization}
import net.liftweb.json.Serialization._
import net.liftweb.json.JsonAST.{JString, JBool, JField, JObject}
import scala.concurrent.{Promise, Future}
import org.scalatest.concurrent.Eventually

case class TestScala[T](f:Future[T])
case class ModelScalaF(str:String, num:Int, f:Future[String])

class ScalaFutureSerializerSpecs extends WordSpec with ShouldMatchers with Eventually {
  import AngularExecutionContext._
  implicit val formats = Serialization.formats(NoTypeHints) + new LAFutureSerializer
  import scala.concurrent.ExecutionContext.Implicits.global

  "A ScalaFutureSerializer" should {
    "map unsatisfied futures to an object with a random ID and state" in {
      val p = Promise[String]()
      val test = TestScala(p.future)
      val json = write(test)
      val back = JsonParser.parse(json)

      back match {
        case JObject(List(JField("f", JObject(List(
          JField("net.liftmodules.ng.Angular.future", JString(id)),
          JField("state", JString("unresolved"))
        ))))) =>
        case _ => fail(back + " did not match as expected")
      }
    }

    "map Failure-satisfied futures to an object with an id, data, and state" in {
      val ex = new Exception("the future failed")
      val f:Future[String] = Future.failed(ex)
      val test = TestScala(f)

      eventually {
        val json = write(test)
        val back = JsonParser.parse(json)

        back match {
          case JObject(List(JField("f", JObject(List(
            JField("net.liftmodules.ng.Angular.future", JString(id)),
            JField("state", JString("rejected")),
            JField("data", JString("the future failed"))
          ))))) =>
          case _ => fail(back+" did not match as expected")
        }
      }

    }

    "map Full[String]-satisfied futures to an object with an id, data, and state" in {
      val f = Future("the data")
      val test = TestScala(f)

      eventually {
        val json = write(test)
        val back = JsonParser.parse(json)

        back match {
          case JObject(List(JField("f", JObject(List(
            JField("net.liftmodules.ng.Angular.future", JString(id)),
            JField("state", JString("resolved")),
            JField("data", JString("the data"))
          ))))) =>
          case _ => fail(back+" did not match as expected")
        }
      }
    }

  }
}
