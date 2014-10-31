package net.liftmodules.ng

import org.scalatest._
import matchers.ShouldMatchers
import net.liftweb.json.{Serialization, NoTypeHints}
import Serialization.write
import net.liftweb.actor.LAFuture
import net.liftweb.common.Box
import net.liftweb.json.JsonAST.JNull

case class Test(f:LAFuture[Box[String]])

class LAFutureSerializerSpecs extends WordSpec with ShouldMatchers {
  implicit val formats = Serialization.formats(NoTypeHints) + new LAFutureSerializer

  "An LAFutureSerializer" should {
    "map futures to JNull" in {
      val test = Test(new LAFuture())
      write(test) should be ("""{"f":null}""")
    }
  }
}
