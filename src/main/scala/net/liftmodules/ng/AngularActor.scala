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
  private def rand = "NG"+randomString(18)
  private val id:String = rand

  /** Render a div for us to hook into */
  def render = <div id={id}></div>

  private val scope = "var scope = angular.element(document.querySelector('#"+id+"')).scope();"

  def broadcast(event:String, obj:AnyRef) = partialUpdate {
    def build(msg:String) = scope+"scope.$apply(function() { scope.$broadcast('"+event+"','"+msg+"') });"
    val cmd = obj match {
      case s:String => build(s)
    }
    JsRaw(cmd)
  }

}
