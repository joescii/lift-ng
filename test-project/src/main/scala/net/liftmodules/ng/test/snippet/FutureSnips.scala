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
      println("Called!!")
      val f = new LAFuture[Box[String]]()
      Schedule.schedule(() => f.satisfy(Full("Server!")), 1 second)
      f
    }))
//    .jsonFuture("getFutureVal", (obj:Test2Obj) => {
//      import obj._
//      logger.info(s"getFutureVal($obj) received on server.")
//      val f = new LAFuture[Test2Obj]()
//      Schedule.schedule(() => {f.satisfy(Test2Obj(s"FromFuture $str1", s"FromFuture $str2"))}, 1 second)
//      f
//    }))
  )
}
