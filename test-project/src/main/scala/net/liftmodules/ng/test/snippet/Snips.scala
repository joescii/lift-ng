package net.liftmodules.ng
package test.snippet

import Angular._
import scala.xml.NodeSeq

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
}
