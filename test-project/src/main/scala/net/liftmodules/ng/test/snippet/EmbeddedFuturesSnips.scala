package net.liftmodules.ng
package test.snippet

import Angular._
import net.liftweb.actor.LAFuture
import net.liftmodules.ng.test.model.StringInt
import net.liftweb.common.{Empty, Failure, Full, Box}
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._
import net.liftweb.http.S

case class EmbeddedFutures(
  resolved: LAFuture[Box[String]],
  failed:   LAFuture[Box[String]],
  string:   LAFuture[Box[String]],
  obj:      LAFuture[Box[StringInt]],
  arr:      List[LAFuture[Box[String]]],
  fobj:     LAFuture[Box[EmbeddedObj]]
)

case class EmbeddedObj(
  resolved: LAFuture[Box[String]],
  failed:   LAFuture[Box[String]],
  string:   LAFuture[Box[String]],
  obj:      LAFuture[Box[StringInt]]
)

object EmbeddedFuturesSnips {
  def services = renderIfNotAlreadyDefined(
    angular.module("EmbeddedFutures")
      .factory("embeddedFutureServices", jsObjFactory()
        .future("fetch", {
          S.session.map(_.sendCometActorMessage("EmbeddedFutureActor", Empty, "go"))
          buildFuture
        })
      )
  )

  def buildFuture = {
    val f = new LAFuture[Box[EmbeddedFutures]]

    val resolved = new LAFuture[Box[String]]
    resolved.satisfy(Full("resolved"))

    val failed = new LAFuture[Box[String]]
    satisfy(failed, Failure("failed"))

    val string = new LAFuture[Box[String]]
    satisfy(string, Full("future"))

    val obj = new LAFuture[Box[StringInt]]
    satisfy(obj, Full(StringInt("string", 42)))

    val arr = List(new LAFuture[Box[String]], new LAFuture[Box[String]])
    satisfy(arr(0), Full("Roll"))
    satisfy(arr(1), Full("Tide!"))

    val fobj = new LAFuture[Box[EmbeddedObj]]
    val fobjResolved = new LAFuture[Box[String]]
    val fobjFailed   = new LAFuture[Box[String]]
    val fobjString   = new LAFuture[Box[String]]
    val fobjObj      = new LAFuture[Box[StringInt]]
    satisfy(fobj, Full(EmbeddedObj(fobjResolved, fobjFailed, fobjString, fobjObj)))
    fobjResolved.satisfy(Full("sub resolved"))
    satisfy(fobjFailed, Failure("sub fail"))
    satisfy(fobjString, Full("sub string"))
    satisfy(fobjObj,    Full(StringInt("sub obj string", 44)))

    val model = EmbeddedFutures(resolved, failed, string, obj, arr, fobj)
    f.satisfy(Full(model))
    f
  }

  def satisfy[T](future:LAFuture[Box[T]], value:Box[T]) {
    def delay = (Math.random() * 3000).toInt.millis
    Schedule(() => {
      future.satisfy(value)
    }, delay)
  }
}
