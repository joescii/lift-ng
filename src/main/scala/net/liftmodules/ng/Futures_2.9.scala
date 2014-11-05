package net.liftmodules.ng

import net.liftweb.json._

object AngularExecutionContext {
}

trait ScalaFutureSerializer extends ScalaUtils {
  def scalaFutureSerializer(formats:Formats):PartialFunction[Any, JValue] = empty_pf
}
