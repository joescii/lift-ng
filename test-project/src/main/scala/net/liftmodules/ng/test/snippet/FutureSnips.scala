package net.liftmodules.ng
package test.snippet

import Angular._
import test.model.Test2Obj

import net.liftweb._
import common._
import actor.LAFuture
import util.Schedule
import util.Helpers._

import scala.xml.NodeSeq

object FutureSnips extends Loggable {
  def services(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("Futures").factory("futureServices", jsObjFactory()
      .future("noArg", {
        val f = new LAFuture[Box[String]]()
        Schedule.schedule(() => f.satisfy(Full("FromFuture")), 1 second)
        f
      })
      .future("failure", {
        val f = new LAFuture[Box[String]]()
        Schedule.schedule(() => f.satisfy(Failure("FailureTest")), 1 second)
        f
      })
      .future("stringArg", (arg:String) => {
        val f = new LAFuture[Box[String]]()
        Schedule.schedule(() => f.satisfy(Full("FromFuture: "+arg)), 1 second)
        f
      })
      .future("jsonArg", (obj:Test2Obj) => {
        import obj._
        val f = new LAFuture[Box[Test2Obj]]()
        Schedule.schedule(() => f.satisfy(Full(Test2Obj(s"FromFuture $str1", s"FromFuture $str2"))), 1 second)
        f
      })
      .future("empty", {
        val f = new LAFuture[Box[String]]
        Schedule.schedule(() => f.satisfy(Empty), 1 second)
        f
      })
      .future("satisfied", {
        val f = new LAFuture[Box[Nothing]]
        f.satisfy(Empty)
        f
      })
      .future("scalaFuture", {
        import FutureConversions._
        import scala.concurrent. { Promise => ScalaPromise, Future }
        import scala.concurrent.ExecutionContext.Implicits.global
        import scala.util.Try

        val p = ScalaPromise[String]()
        Schedule.schedule(() => p.complete(Try("ScalaFuture")), 1 second)
        p.future.la
      })
    )
  )
}
