package net.liftmodules.ng

import net.liftweb.json._
import net.liftweb.json.Serialization._

package object js {
  implicit def ToWithExtractMerged(jVal:JValue) = new WithExtractMerged(jVal)

  class WithExtractMerged(jVal:JValue) {
    implicit val formats = DefaultFormats

    def extractMerged[T <: AnyRef](defaults:T)(implicit mf:Manifest[T]):T = {
      val jDef = parse(write(defaults))
      val jRes = jDef merge jVal
      jRes.extract[T]
    }
  }
}
