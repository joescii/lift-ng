package net.liftmodules.ng

import org.scalatest._
import net.liftweb.json.{JsonParser, Serialization, NoTypeHints}
import Serialization.write
import net.liftweb.actor.LAFuture
import net.liftweb.common._
import net.liftweb.json.JsonAST._

case class Test[T](f:LAFuture[Box[T]])
case class Model(str:String, num:Int)
case class ModelF(str:String, num:Int, f:LAFuture[Box[String]])

class LAFutureSerializerSpecs extends WordSpec with Matchers {
  import AngularExecutionContext._
  implicit val formats = Serialization.formats(NoTypeHints) + new LAFutureSerializer

  "An LAFutureSerializer" should {
    "map unsatisfied futures to an object with a random ID and state" in {
      val test = Test[String](new LAFuture())
      val json = write(test)
      val back = JsonParser.parse(json)

      back match {
        case JObject(List(JField("f", JObject(List(
          JField("net.liftmodules.ng.Angular.future", JString(id)),
          JField("state", JString("pending"))
        ))))) =>
        case _ => fail(back+" did not match as expected")
      }
    }

    "map Empty-satisfied futures to an object with an id and state" in {
      val f = new LAFuture[Box[String]]
      val test = Test(f)
      f.satisfy(Empty)

      val json = write(test)
      val back = JsonParser.parse(json)

      back match {
        case JObject(List(JField("f", JObject(List(
          JField("net.liftmodules.ng.Angular.future", JString(id)),
          JField("state", JString("resolved"))
        ))))) =>
        case _ => fail(back+" did not match as expected")
      }
    }

    "map Failure-satisfied futures to an object with an id, data, and state" in {
      val f = new LAFuture[Box[String]]
      val test = Test(f)
      f.satisfy(Failure("this failed"))

      val json = write(test)
      val back = JsonParser.parse(json)

      back match {
        case JObject(List(JField("f", JObject(List(
          JField("net.liftmodules.ng.Angular.future", JString(id)),
          JField("state", JString("rejected")),
          JField("data", JString("this failed"))
        ))))) =>
        case _ => fail(back+" did not match as expected")
      }
    }

    "map Full[String]-satisfied futures to an object with an id, data, and state" in {
      val f = new LAFuture[Box[String]]
      val test = Test(f)
      f.satisfy(Full("the data"))

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

    "map Full[Model]-satisfied futures to an object with an id, data, and state" in {
      val f = new LAFuture[Box[Model]]
      val test = Test(f)
      f.satisfy(Full(Model("a string", 42)))

      val json = write(test)
      val back = JsonParser.parse(json)

      back match {
        case JObject(List(JField("f", JObject(List(
          JField("net.liftmodules.ng.Angular.future", JString(id)),
          JField("state", JString("resolved")),
          JField("data", JObject(List(
            JField("str", JString("a string")),
            JField("num", JInt(int))
          )))
        ))))) => int should equal (42)
        case _ => fail(back+" did not match as expected")
      }
    }

    "map Full[ModelF]-satisfied futures to an object with an id, data (with an embedded future), and state" in {
      val f = new LAFuture[Box[ModelF]]
      val test = Test(f)
      val fString = new LAFuture[Box[String]]
      f.satisfy(Full(ModelF("another string", 43, fString)))

      val json = write(test)
      val back = JsonParser.parse(json)

      back match {
        case JObject(List(JField("f", JObject(List(
          JField("net.liftmodules.ng.Angular.future", JString(id1)),
          JField("state", JString("resolved")),
          JField("data", JObject(List(
            JField("str", JString("another string")),
            JField("num", JInt(int)),
            JField("f", JObject(List(
              JField("net.liftmodules.ng.Angular.future", JString(id2)),
              JField("state", JString("pending"))
            )))
          )))
        ))))) => int should equal (43)
        case _ => fail(back+" did not match as expected")
      }
    }

  }
}
