package net.liftmodules.ng

import net.liftweb._
import http._
import common._
import util._
import http.js._
import JE._
import JsCmds._
import StringHelpers._
import json.Serialization._
import json.DefaultFormats
import json.JsonAST._

/** A comet actor for Angular action */
trait AngularActor extends CometActor with Loggable {
  private def rand = "NG"+randomString(18)
  private val id:String = rand

  /** Render a div for us to hook into */
  def render = <div id={id}></div>

  private implicit val formats = DefaultFormats // Some crap needed for stringify
  protected def stringify(obj:AnyRef):String = obj match {
      case s:String => "'"+s+"'"
      case _ => write(obj)
    }

  trait Scope {
    // TODO: Use an Int and change this to obj:Any??
    /** Performs a <code>\$broadcast()</code> with the given event name and object argument */
    def broadcast(event:String, obj:AnyRef):Unit = partialUpdate(eventCmd("broadcast", event, obj))
    /** Performs a <code>\$emit()</code> with the given event name and object argument */
    def emit(event:String, obj:AnyRef):Unit = partialUpdate(eventCmd("emit", event, obj))
    /** Performs assignment of the second argument to the scope variable/field specified in the first argument */
    def assign(field:String, obj:AnyRef):Unit = partialUpdate(assignCmd(field, obj))

    /** Variables needed to perform any of our angular actions (will be \$scope and possibly \$rootScope) */
    protected def vars:JsCmd
    /** The variable name of the scope variable for this scope (either \$scope or \$rootScope) */
    protected def scopeVar:String

    /** Interval between tries to unload our early-arrival event queue */
    private val interval = Props.getInt("net.liftmodules.ng.AngularActor.retryInterval", 100)

    protected val varElement = JsCrVar("e", Call("angular.element", Call("document.querySelector", JString("#"+id))))// "var s=angular.element(document.querySelector('#"+id+"')).scope();"
    /** Variable assignment for \$scope */
    protected val varScope = JsCrVar("s", AnonFunc(JsReturn(Call("e.scope"))))
    /** Variable assignment for \$rootScope */
    protected val varRoot  = JsCrVar("r", AnonFunc(JsReturn(JsRaw("(typeof s()==='undefined')?void 0:s().$root"))))// "var r=(typeof s==='undefined')?void 0:s.$root;"

    /** Sends an event command, i.e. broadcast or emit */
    private def eventCmd(method:String, event:String, obj:AnyRef):JsCmd = {
      doCmd(scopeVar+".$apply(function(){"+scopeVar+".$"+method+"('"+event+"',"+stringify(obj)+");});")
    }

    /** Sends an assignment command */
    private def assignCmd(field:String, obj:AnyRef):JsCmd = {
      doCmd(scopeVar+".$apply(function(){"+scopeVar+"."+field+"="+stringify(obj)+";});")
    }

    /** Sends any of our commands with all of the early-arrival retry mechanism packaged up */
    private def doCmd(f:String):JsCmd = {
      val ready = "var t=function(){return typeof " + scopeVar + "!=='undefined';};"
      val fn = "var f=function(){"+f+"};"
      val dequeue = "var d=function(){" +
        "if(net_liftmodules_ng_q[0].t()){"+
          "for(i=0;i<net_liftmodules_ng_q.length;i++){" +
            "net_liftmodules_ng_q[i].f();"+
          "}"+
          "net_liftmodules_ng_q=void 0;"+
        "}else{"+
          "setTimeout(function(){d();},"+interval+");"+
        "}"+
      "};"
      val enqueue = "if(typeof net_liftmodules_ng_q==='undefined'){net_liftmodules_ng_q=[];setTimeout(function(){d();},"+interval+");}" +
        "net_liftmodules_ng_q.push({t:t,f:f});"
      vars & JsRaw(ready+fn+dequeue+"if(typeof net_liftmodules_ng_q==='undefined'&&t()){f();}else{"+enqueue+"}")
    }
  }

  private class ChildScope extends Scope {
    override val vars = varElement&varScope
    override val scopeVar = "s()"
  }

  /** Your handle to the \$scope object for your actor */
  val scope:Scope = new ChildScope

  /** Your handle to the \$rootScope object for your actor */
  object rootScope extends Scope {
    override val vars = varElement&varScope&varRoot
    override val scopeVar = "r()"
  }

}
