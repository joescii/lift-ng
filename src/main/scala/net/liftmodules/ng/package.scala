package net.liftmodules

import net.liftmodules.ng.Angular.{Promise, Reject, Resolve}
import net.liftweb.common._
import net.liftweb.json.JsonAST.{JField, JObject, JString}
import net.liftweb.util.StringHelpers._

import scala.concurrent.Future

package object ng extends Loggable {
  private [ng] def throwableToFailure(t: Throwable): Failure =  {
    logger.warn("Uncaught exception while processing ajax function", t)
    Failure(t.getMessage, Full(t), Empty)
  }

  private [ng] def promiseToJson(promise: Promise): JObject = {
    promise match {
      case Resolve(Some(jsExp), _) => JObject(List(JField("state", JString("resolved")), JField("data", jsExp)))
      case Resolve(None, None) => JObject(List(JField("state", JString("resolved"))))
      case Resolve(None, Some(futureId)) => JObject(List(JField("state", JString("pending")), JField("futureId", JString(futureId))))
      case Reject(data) => JObject(List(JField("state", JString("rejected")), JField("data", data)))
    }
  }

  // We convert all of our futures (LAFuture or Future) to this type. Hence anywhere you see this type alias,
  // we expect it to be a future which does not fail but instead successfully has its results (good or bad) in a Box.
  // Note that we've done nothing here to compile-time guarantee that behavior.
  type FutureBox[T] = Future[Box[T]]
}
