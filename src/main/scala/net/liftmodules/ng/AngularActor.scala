package net.liftmodules.ng

import net.liftweb._
import http._
import common._
import util._
import js._
import JE._
import JsCmds._
import StringHelpers._

/** A comet actor for Angular action */
trait AngularActor extends CometActor with Loggable {
  def rand = "NG"+randomString(18)
  val module:String = rand
  val directive:String = rand
  val id:String = rand

  def render = <div id={id}></div>

  val scope = "var scope = angular.element(document.querySelector('#"+id+"')).scope();"

  def broadcast(event:String, msg:String) = partialUpdate {
    val cmd = scope+"scope.$apply(function() { scope.$broadcast('"+event+"','"+msg+"') });"
    JsRaw(cmd)
  }

  def emit(event:String, msg:String) = partialUpdate {
    val cmd = scope+"scope.$apply(function() { scope.$emit('"+event+"','"+msg+"') });"
    JsRaw(cmd)
  }
}
