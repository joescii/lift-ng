package net.liftmodules

import net.liftmodules.ng.Angular.{Promise, Reject, Resolve}
import net.liftweb.common.{Empty, Failure, Full, Loggable}
import net.liftweb.json.JsonAST.{JField, JObject, JString}
import net.liftweb.util.StringHelpers._

package object ng extends Loggable {
  private [ng] def throwableToFailure(t: Throwable): Failure =  {
    logger.warn("Uncaught exception while processing ajax function", t)
    val msg = encJs(t.getMessage).drop(1).dropRight(1) // Encode into valid JS, but strip the quotes it adds
    Failure(msg, Full(t), Empty)
  }

  private [ng] def promiseToJson(promise: Promise): JObject = {
    promise match {
      case Resolve(Some(jsExp), _) => JObject(List(JField("state", JString("resolved")), JField("data", jsExp)))
      case Resolve(None, None) => JObject(List(JField("state", JString("resolved"))))
      case Resolve(None, Some(futureId)) => JObject(List(JField("state", JString("unresolved")), JField("futureId", JString(futureId))))
      case Reject(data) => JObject(List(JField("state", JString("rejected")), JField("data", data)))
    }
  }
}
