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

  /** Your handle to the \$rootScope object for your actor */
  object rootScope {
    private val varRootScope = "var rootScope = angular.element(document.querySelector('#"+id+"')).scope().$root;"
    implicit val formats = DefaultFormats

    private def cmdWithArg(cmdBuilder: String => String, obj:AnyRef) = obj match {
      case s:String => cmdBuilder("'"+s+"'")
      case _ => cmdBuilder(write(obj))
    }

    private def messenger(method:String)(event:String, obj:AnyRef):Unit = partialUpdate {
      def cmdBuilder(arg:String) = varRootScope+"rootScope.$apply(function() { rootScope."+method+"('"+event+"',"+arg+") });"
      val cmd = cmdWithArg(cmdBuilder, obj)
      logger.debug("Emitting JsRaw: "+cmd)
      JsRaw(cmd)
    }


    /** Performs a <code>\$rootScope.\$broadcast()</code> with the given event name and object argument */
    def broadcast = messenger("$broadcast")_

    /** Performs a <code>\$rootScope.\$emit()</code> with the given event name and object argument */
    def emit = messenger("$emit")_
  }

}
