package net.liftmodules.ng

import net.liftweb.json._
import net.liftweb.json.Serialization._

package object js extends LiftNgJsHelpers {
  implicit def ToWithExtractMerged(jVal:JValue) = new WithExtractMerged(jVal)

  class WithExtractMerged(jVal:JValue) {
    def extractMerged[T <: AnyRef](defaults:T)(implicit mf:Manifest[T], formats:Formats):T = {
      val jDef = parse(stringify(defaults))
      val jRes = jDef merge jVal
      jRes.extract[T]
    }
  }
}
