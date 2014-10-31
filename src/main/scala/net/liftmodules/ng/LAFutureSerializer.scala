package net.liftmodules.ng

import Angular.NgModel

import net.liftweb._
import json._
import actor.LAFuture
import common._

class LAFutureSerializer[T <: NgModel : Manifest] extends CustomSerializer[LAFuture[Box[T]]](format => (
{
  case JObject(List(JField("net.liftmodules.ng.Angular.futureId", JString(id)))) => {
    val future = new LAFuture[Box[T]]()
    future.abort()
    future
  }
},
{
  case future:LAFuture[Box[T]] => JNull
}
))
