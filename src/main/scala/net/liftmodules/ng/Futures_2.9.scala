package net.liftmodules.ng

import net.liftweb.json._
import net.liftmodules.ng.Angular.NgModel

object AngularExecutionContext {

}

class LAFutureSerializer[T <: NgModel](implicit m: Manifest[T]) extends LAFutureSerializerBase[T] {
  override val futureSerializer = new LAFutureSerializer[T]

  def serialize(implicit format: Formats) = laFutureSerializer(format)
}