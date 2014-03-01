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

    /** Performs a \$rootScope.\$broadcast with the given event name and object argument */
    def broadcast(event:String, obj:AnyRef) = partialUpdate {
      implicit val formats = DefaultFormats

      def build(msg:String) = varRootScope+"rootScope.$apply(function() { rootScope.$broadcast('"+event+"',"+msg+") });"
      val cmd = obj match {
        case s:String => build("'"+s+"'")
        case _ => build(write(obj))
      }
      logger.debug("Emitting JsRaw: "+cmd)
      JsRaw(cmd)
    }
  }

}
