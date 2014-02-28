package net.liftmodules.ng
package test.snippet

import Angular._
import scala.xml.NodeSeq
import test.model.Test2Obj

import net.liftweb.common.{Loggable, Empty, Full}

/** Defines snippets for testing Angular */
object Snips extends Loggable {
  def renderPair(xhtml:NodeSeq) = renderIfNotAlreadyDefined(angular.module("SnipServices1")
    .factory("snipServices1", jsObjFactory()

    .jsonCall("call1", {
      logger.info("call1() received on server")
      Full("FromServer")

  }).jsonCall("call2", (str:String) => {
      logger.info(s"call2($str) received on server.")
      Full(s"FromServer $str")
  })))

  def renderSingle(xhtml:NodeSeq) = renderIfNotAlreadyDefined(angular.module("SnipServices2")
    .factory("snipServices2", jsObjFactory()

    .jsonCall("call", (obj:Test2Obj) => {
      import obj._
      logger.info(s"call($obj) received on server.")
      Full(Test2Obj(s"FromServer $str1", s"FromServer $str2"))
  })))
}
