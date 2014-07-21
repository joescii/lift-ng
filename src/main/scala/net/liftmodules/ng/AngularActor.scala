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
import scala.xml.NodeSeq
import net.liftweb.json.JsonAST.JString

/** A comet actor for Angular action */
trait AngularActor extends CometActor with Loggable {
  private def rand = "NG"+randomString(18)
  private val id:String = rand

  val nodesToRender:NodeSeq = <div id={id}></div>

  /** Render a div for us to hook into */
  def render = nodesToRender

  private implicit val formats = DefaultFormats // Some crap needed for stringify
  protected def stringify(obj:AnyRef):String = obj match {
      case s:String => "'"+s+"'"
      case _ => write(obj)
    }

  /** Interval between tries to unload our early-arrival event queue */
  private val interval = Props.getInt("net.liftmodules.ng.AngularActor.retryInterval", 100)

  private val varElement = JsCrVar("e", Call("angular.element", Call("document.querySelector", JString("#"+id))))// "var s=angular.element(document.querySelector('#"+id+"')).scope();"
  /** Variable assignment for \$scope */
  protected val scopeExp = "e.scope()"
  /** Variable assignment for \$rootScope */
  protected val rootExp  = "((typeof "+scopeExp+"==='undefined')?void 0:"+scopeExp+".$root)"

  def targetExp(root:Boolean) = (if(root) rootExp else scopeExp)

  /** Sends any of our commands with all of the early-arrival retry mechanism packaged up */
  protected def buildCmd(root:Boolean, f:JsCmd):JsCmd = {
    val ready = JsCrVar("t", AnonFunc(JsReturn(JsRaw("typeof e.scope()!=='undefined'"))))
    val fn = JsCrVar("f", AnonFunc(Call("("+targetExp(root)+").$apply", AnonFunc(f))))
    val dequeue = "var d=function(){" +
      "if(net_liftmodules_ng_q[0].t()){"+
        "for(i=0;i<net_liftmodules_ng_q.length;i++){" +
           "net_liftmodules_ng_q[i].f();"+
          "}"+
        "net_liftmodules_ng_q=void 0;"+
      "}else{console.log('resetting timeout');"+
        "setTimeout(function(){d();},"+interval+");"+
      "}"+
    "};"
    val enqueue = "if(typeof net_liftmodules_ng_q==='undefined'){net_liftmodules_ng_q=[];setTimeout(function(){d();},"+interval+");}" +
      "net_liftmodules_ng_q.push({t:t,f:f});"
    val cmds = varElement & ready & fn & JsRaw(dequeue+"if(typeof net_liftmodules_ng_q==='undefined'&&t()){f();}else{"+enqueue+"}")
    logger.debug(cmds)
    cmds
  }

  trait Scope {
    // TODO: Use an Int and change this to obj:Any??
    /** Performs a <code>\$broadcast()</code> with the given event name and object argument */
    def broadcast(event:String, obj:AnyRef):Unit = partialUpdate(eventCmd("broadcast", event, obj))
    /** Performs a <code>\$emit()</code> with the given event name and object argument */
    def emit(event:String, obj:AnyRef):Unit = partialUpdate(eventCmd("emit", event, obj))
    /** Performs assignment of the second argument to the scope variable/field specified in the first argument */
    def assign(field:String, obj:AnyRef):Unit = partialUpdate(assignCmd(field, obj))

    protected def root:Boolean

    /** Sends an event command, i.e. broadcast or emit */
    private def eventCmd(method:String, event:String, obj:AnyRef):JsCmd = {
      buildCmd(root, JsRaw(targetExp(root)+".$"+method+"('"+event+"',"+stringify(obj)+")"))
    }

    /** Sends an assignment command */
    private def assignCmd(field:String, obj:AnyRef):JsCmd = {
      buildCmd(root, JsRaw(targetExp(root)+"."+field+"="+stringify(obj)))
    }
  }

  private class ChildScope extends Scope {
    override val root = false
  }

  /** Your handle to the \$scope object for your actor */
  val scope:Scope = new ChildScope

  /** Your handle to the \$rootScope object for your actor */
  object rootScope extends Scope {
    override val root = true
  }

}
