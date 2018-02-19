package net.liftmodules.ng
package test.snippet

import Angular._
import test.model.Test2Obj
import net.liftweb._
import common._
import actor.LAFuture
import net.liftweb.json.DefaultFormats
import util.Schedule
import util.Helpers._

import scala.xml.NodeSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise => SPromise }

object FutureSnips extends Loggable {
  implicit val formats = DefaultFormats

  def services(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("Futures").factory("futureServices", jsObjFactory()
      .defFutureAny("noArg", {
        val p = SPromise[String]()
        Schedule.schedule(() => p.success("FromFuture"), 1 second)
        p.future
      })
      .defFutureAny("exception", {
        throw new Exception("FromServerFutureException")
      }.asInstanceOf[Future[Any]])
      .defParamToFutureAny("stringArg", (arg: String) => {
        val p = SPromise[String]()
        Schedule.schedule(() => p.success("FromFuture: "+arg), 1 second)
        p.future
      })
      .defParamToFutureAny("jsonArg", (obj: Test2Obj) => {
        import obj._
        val p = SPromise[Test2Obj]()
        Schedule.schedule(() => p.success(Test2Obj(s"FromFuture $str1", s"FromFuture $str2")), 1 second)
        p.future
      })
      .defFutureAny("empty", {
        val p = SPromise[Box[String]]()
        Schedule.schedule(() => p.success(Empty), 1 second)
        p.future
      })
      .defFutureAny("satisfied", {
        Future { Empty }
      })
      .future("scalaFuture", {
        import FutureConversions._
        import scala.concurrent. { Promise => ScalaPromise, Future }
        import scala.util.Try

        val p = ScalaPromise[String]()
        Schedule.schedule(() => p.complete(Try("ScalaFuture")), 1 second)
        p.future.la
      })
    )
  )
}
