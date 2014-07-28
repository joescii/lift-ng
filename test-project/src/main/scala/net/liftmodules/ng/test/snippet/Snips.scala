package net.liftmodules.ng
package test.snippet

import Angular._
import scala.xml.NodeSeq
import test.model.Test2Obj

import net.liftweb._
import net.liftweb.common.{Failure, Loggable, Empty, Full}
import util._
import http._
import js._
import Helpers._
import SHtml._
import JsCmds._

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
  }).jsonCall("callFail", {
      logger.info("callFail() received on server")
      Failure("FromServerFail")
  })))

  def renderSingle(xhtml:NodeSeq) = renderIfNotAlreadyDefined(angular.module("SnipServices2")
    .factory("snipServices2", jsObjFactory()

    .jsonCall("call", (obj:Test2Obj) => {
      import obj._
      logger.info(s"call($obj) received on server.")
      Full(Test2Obj(s"FromServer $str1", s"FromServer $str2"))
  })))

  def renderRootScopeBroadcastStringButton = "* [onclick]" #> ajaxInvoke( () => {
    S.session.map { _.sendCometActorMessage("RootScopeBroadcastStringActor", Empty, "start") }
    Noop
  })

  def renderRootScopeBroadcastJsonButton = "* [onclick]" #> ajaxInvoke( () => {
    S.session.map { _.sendCometActorMessage("RootScopeBroadcastJsonActor", Empty, "start") }
    Noop
  })

  def renderRootScopeEmitStringButton = "* [onclick]" #> ajaxInvoke( () => {
    S.session.map { _.sendCometActorMessage("RootScopeEmitStringActor", Empty, "start") }
    Noop
  })

  def renderRootScopeEmitJsonButton = "* [onclick]" #> ajaxInvoke( () => {
    S.session.map { _.sendCometActorMessage("RootScopeEmitJsonActor", Empty, "start") }
    Noop
  })

  def renderScopeEmitButton = "* [onclick]" #> ajaxInvoke( () => {
    S.session.map { _.sendCometActorMessage("ScopeActor", Empty, "emit") }
    Noop
  })

  def renderScopeBroadcastButton = "* [onclick]" #> ajaxInvoke( () => {
    S.session.map { _.sendCometActorMessage("ScopeActor", Empty, "broadcast") }
    Noop
  })

  def renderCometAssignmentButton = "* [onclick]" #> ajaxInvoke( () => {
    S.session.map { _.sendCometActorMessage("AssignmentActor", Empty, "start") }
    Noop
  })

  def renderCometDelayButton = "* [onclick]" #> ajaxInvoke( () => {
    S.session.map { _.sendCometActorMessage("EarlyEmitActor", Empty, "go") }
    Noop
  })

}
