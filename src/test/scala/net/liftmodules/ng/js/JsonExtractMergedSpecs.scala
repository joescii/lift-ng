package net.liftmodules.ng.js

import org.scalatest._
import net.liftweb.json._

case class Test(a:String, b:Int)

class JsonExtractMergedSpecs extends FlatSpecLike with Matchers {
  implicit val formats = DefaultFormats

  "This code" should "work" in {
    val t = Test("string", 5)
    val json = """ {"b":7} """
    val jVal = parse(json)
    val t2 = jVal.extractMerged(t)

    t2 should be (Test("string", 7))
  }
}
