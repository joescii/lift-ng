package net.liftmodules.ng
package test.snippet

import Angular._
import net.liftweb.actor.LAFuture
import net.liftmodules.ng.test.model.StringInt
import net.liftweb.common.{Empty, Failure, Full, Box}
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scala.concurrent.{ Future, Promise => ScalaPromise }
import scala.util.Try

case class EmbeddedFutures(
  resolved: LAFuture[Box[String]],
  failed:   LAFuture[Box[String]],
  string:   LAFuture[Box[String]],
  obj:      LAFuture[Box[StringInt]],
  arr:      List[LAFuture[Box[String]]],
  fobj:     LAFuture[Box[EmbeddedObj]]
) extends NgModel

case class EmbeddedObj(
  resolved: LAFuture[Box[String]],
  failed:   LAFuture[Box[String]],
  string:   LAFuture[Box[String]],
  obj:      LAFuture[Box[StringInt]]
) extends NgModel

case class EmbeddedScalaFutures(
  resolved: Future[String],
  failed:   Future[String],
  string:   Future[String],
  obj:      Future[StringInt]
) extends NgModel

object EmbeddedFuturesSnips {
  def services = renderIfNotAlreadyDefined(
    angular.module("EmbeddedFutures")
      .factory("embeddedFutureServices", jsObjFactory()
        .future("fetch", {
          S.session.map(_.sendCometActorMessage("EmbeddedFutureActor", Empty, "go"))
          buildFuture
        })
        .jsonCall("sfetch", { Full(buildScalaModel) })
      )
  )

  def buildModel = {
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

    EmbeddedFutures(resolved, failed, string, obj, arr, fobj)
  }

  def buildFuture = {
    val f = new LAFuture[Box[EmbeddedFutures]]
    f.satisfy(Full(buildModel))
    f
  }

  def sched(f: => Unit) = {
    def delay = (Math.random() * 3000).toInt.millis
    Schedule(() => { f }, delay)
  }

  def satisfy[T](future:LAFuture[Box[T]], value:Box[T]) {
    sched( future.satisfy(value) )
  }

  def satisfy[T](p:ScalaPromise[T], value:T) {
    sched( p.complete(Try(value)) )
  }

  def buildScalaModel = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val resolved = Future("resolved")
    val failed = ScalaPromise[String]()
    sched( failed.failure(new Exception("failed")) )

    val string = ScalaPromise[String]()
    satisfy(string, "future")

    val obj = ScalaPromise[StringInt]()
    satisfy(obj, StringInt("string", 42))

    EmbeddedScalaFutures(resolved, failed.future, string.future, obj.future)
  }
}
