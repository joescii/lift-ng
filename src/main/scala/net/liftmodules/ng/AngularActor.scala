package net.liftmodules.ng

import net.liftweb._
import http._
import common._
import util._
import js._
import JE._
import JsCmds._
import StringHelpers._
import json.Serialization._
import json.DefaultFormats

/** A comet actor for Angular action */
trait AngularActor extends CometActor with Loggable {
  private def rand = "NG"+randomString(18)
  private val id:String = rand

  /** Render a div for us to hook into */
  def render = <div id={id}></div>

  trait Scope {
    // TODO: Use an Int and change this to obj:Any??
    /** Performs a <code>\$broadcast()</code> with the given event name and object argument */
    def broadcast(event:String, obj:AnyRef):Unit = partialUpdate(eventCmd(scopeVar, "broadcast", event, obj))
    /** Performs a <code>\$emit()</code> with the given event name and object argument */
    def emit(event:String, obj:AnyRef):Unit = partialUpdate(eventCmd(scopeVar, "emit", event, obj))

    protected def vars:String
    protected def scopeVar:String

    protected val varScope = "var s=angular.element(document.querySelector('#"+id+"')).scope();"
    protected val varRoot  = "var r=s.$root;"
    implicit val formats = DefaultFormats

    protected def stringify(obj:AnyRef):String = obj match {
      case s:String => "'"+s+"'"
      case _ => write(obj)
    }

    protected def eventInvoke(scopeVar:String, method:String, event:String, obj:AnyRef):String =
      scopeVar+".$apply(function(){"+scopeVar+".$"+method+"('"+event+"',"+stringify(obj)+")});"

    protected def eventCmd(scopeVar:String, method:String, event:String, obj:AnyRef):JsCmd =
      JsRaw(vars+eventInvoke(scopeVar, method, event, obj))
  }

  private class ChildScope extends Scope {
    override val vars = varScope
    override val scopeVar = "s"
  }

  /** Your handle to the \$scope object for your actor */
  val scope:Scope = new ChildScope

  /** Your handle to the \$rootScope object for your actor */
  object rootScope extends Scope {
    override val vars = varScope+varRoot
    override val scopeVar = "r"
  }

}
