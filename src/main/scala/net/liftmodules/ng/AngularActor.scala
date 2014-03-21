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
    /** Performs assigns the second argument to the field specified in the first argument */
    def assign(field:String, obj:AnyRef):Unit = partialUpdate(assignCmd(scopeVar, field, obj))

    /** Variables needed to perform any of our angular actions (will be \$scope and possibly \$rootScope) */
    protected def vars:String
    /** The variable name of the scope variable for this scope (either \$scope or \$rootScope) */
    protected def scopeVar:String

    /** Variable assignment for \$scope */
    protected val varScope = "var s=angular.element(document.querySelector('#"+id+"')).scope();"
    /** Variable assignment for \$rootScope */
    protected val varRoot  = "var r=s.$root;"

    private implicit val formats = DefaultFormats // Some crap needed for stringify
    private def stringify(obj:AnyRef):String = obj match {
      case s:String => "'"+s+"'"
      case _ => write(obj)
    }

    private def eventInvoke(scopeVar:String, method:String, event:String, obj:AnyRef):String =
      scopeVar+".$apply(function(){"+scopeVar+".$"+method+"('"+event+"',"+stringify(obj)+");});"

    private def eventCmd(scopeVar:String, method:String, event:String, obj:AnyRef):JsCmd =
      JsRaw(vars+eventInvoke(scopeVar, method, event, obj))

    private def assignCmd(scopeVar:String, field:String, obj:AnyRef):JsCmd = {
      val assignment = scopeVar+".$apply(function(){"+scopeVar+"."+field+"="+stringify(obj)+";});"
      JsRaw(vars+assignment)
    }
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
