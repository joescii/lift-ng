package net.liftmodules.ng

import org.scalatest._
import matchers.ShouldMatchers
import net.liftweb.json.{JsonParser, Serialization, NoTypeHints}
import Serialization.write
import net.liftweb.actor.LAFuture
import net.liftweb.common.Box
import net.liftweb.json.JsonAST._

case class Test(f:LAFuture[Box[String]])

class LAFutureSerializerSpecs extends WordSpec with ShouldMatchers {
  implicit val formats = Serialization.formats(NoTypeHints) + new LAFutureSerializer

  "An LAFutureSerializer" should {
    "map futures to JNull" in {
      val test = Test(new LAFuture())
      val json = write(test)
      val back = JsonParser.parse(json)

      back match {
        case JObject(List(JField("f", JObject(List(JField("net.liftmodules.ng.Angular.futureId", JString(id))))))) =>
        case _ => fail(back+" did not match as expected")
      }

    }
  }
}
