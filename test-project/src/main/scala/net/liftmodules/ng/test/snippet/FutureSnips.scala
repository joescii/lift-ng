package net.liftmodules.ng
package test.snippet

import Angular._
import test.model.Test2Obj

import net.liftweb.common._

import scala.xml.NodeSeq

object FutureSnips extends Loggable {
  def services(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("Futures").factory("futureServices", jsObjFactory().jsonCall("getFutureVal", (obj:Test2Obj) => {
      import obj._
      logger.info(s"call($obj) received on server.")
      Empty
    }))
  )
}
