package net.liftmodules.ng

import net.liftweb._
import http._
import common._
import util._
import http.js._
import JE._
import JsCmds._
import json.Serialization._
import json.DefaultFormats
import scala.xml.NodeSeq
import net.liftweb.json.JsonAST.JString

private [ng] trait LiftNgJsHelpers extends Loggable {
  protected val id:String = Angular.rand

  /** Interval between tries to unload our early-arrival event queue */
  private val interval = Props.getInt("net.liftmodules.ng.AngularActor.retryInterval", 100)

  /** Variable assignment for \$scope */
  private val varScope = JsCrVar("e", AnonFunc(
    JsRaw(
      "if(typeof angular==='undefined'||typeof angular.element==='undefined')return void 0;else " +
        "return angular.element(document.querySelector('#"+id+"'))"
    )
  )) & JsCrVar("s", AnonFunc(
    JsRaw(
      "if(typeof e()==='undefined')return void 0;else " +
        "return e().scope()"
    )
  ))
  /** Variable assignment for \$rootScope */
  private val varRoot  = JsCrVar("r", AnonFunc(JsReturn(JsRaw("(typeof s()==='undefined')?void 0:s().$root"))))

  /** Sends any of our commands with all of the early-arrival retry mechanism packaged up */
  protected def buildCmd(root:Boolean, f:JsCmd):JsCmd = {
    val scopeFn = if(root) "r()" else "s()"
    val vars = varScope & (if(root) varRoot else Noop)
    val ready = JsCrVar("t", AnonFunc(JsReturn(JsRaw("typeof " + scopeFn + "!=='undefined'"))))
    val fn = JsCrVar("f", AnonFunc(Call(scopeFn+".$apply", AnonFunc(f))))
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
    val cmds = vars & ready & fn & JsRaw(dequeue+"if(typeof net_liftmodules_ng_q==='undefined'&&t()){f();}else{"+enqueue+"}")
    logger.trace(cmds)
    cmds
  }

  import AngularExecutionContext._
  private implicit val formats = DefaultFormats + new LAFutureSerializer
  protected def stringify(obj:AnyRef):String = obj match {
    case s:String => StringHelpers.encJs(s)
    case _ => write(obj)
  }
}

/** A comet actor for Angular action */
trait AngularActor extends CometActor with LiftNgJsHelpers {

  val nodesToRender:NodeSeq = <div id={id}></div>

  def render = NodeSeq.Empty

  /** Render a div for us to hook into */
  override def fixedRender = nodesToRender


  trait Scope {
    // TODO: Use an Int and change this to obj:Any??
    /** Performs a <code>\$broadcast()</code> with the given event name and object argument */
    def broadcast(event:String, obj:AnyRef):Unit = partialUpdate(eventCmd("broadcast", event, obj))
    /** Performs a <code>\$emit()</code> with the given event name and object argument */
    def emit(event:String, obj:AnyRef):Unit = partialUpdate(eventCmd("emit", event, obj))
    /** Performs assignment of the second argument to the scope variable/field specified in the first argument */
    def assign(field:String, obj:AnyRef):Unit = partialUpdate(assignCmd(field, obj))

    protected def root:Boolean
    private def scopeVar = if(root) "r()" else "s()"

    protected def model(obj:AnyRef) = JsCrVar("m", JsRaw(stringify(obj))) & Call("e().injector().get('plumbing').inject", JsVar("m"))

    /** Sends an event command, i.e. broadcast or emit */
    private def eventCmd(method:String, event:String, obj:AnyRef):JsCmd = {
      buildCmd(root, model(obj) & JsRaw(scopeVar+".$"+method+"('"+event+"',m)"))
    }

    /** Sends an assignment command */
    private def assignCmd(field:String, obj:AnyRef):JsCmd = {
      buildCmd(root, model(obj) & JsRaw(scopeVar+"."+field+"="+stringify(obj)))
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
