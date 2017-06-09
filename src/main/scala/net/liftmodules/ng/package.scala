package net.liftmodules

import net.liftmodules.ng.Angular.{Promise, Reject, Resolve}
import net.liftweb.common.{Empty, Failure, Full, Loggable}
import net.liftweb.http.js.JE.JsObj
import net.liftweb.http.js.JsObj
import net.liftweb.util.StringHelpers._

package object ng extends Loggable {
  private [ng] def throwableToFailure(t: Throwable): Failure =  {
    logger.warn("Uncaught exception while processing ajax function", t)
    val msg = encJs(t.getMessage).drop(1).dropRight(1) // Encode into valid JS, but strip the quotes it adds
    Failure(msg, Full(t), Empty)
  }

  private [ng] def promiseToJson(promise: Promise): JsObj = {
    promise match {
      case Resolve(Some(jsExp), _) => JsObj("state" -> "resolved", "data" -> jsExp)
      case Resolve(None, None) => JsObj("state" -> "resolved")
      case Resolve(None, Some(futureId)) => JsObj("state" -> "unresolved", "futureId" -> futureId)
      case Reject(data) => JsObj("state" -> "rejected", "data" -> data)
    }
  }
}
