package net.liftmodules.ng
package comet

import Angular.ReturnData

import net.liftweb._
import http._
import common._
import js._
import JE._
import JsCmds._

class LiftNgFutureActor extends AngularActor {
  def callback(p:Promise) = partialUpdate {
    val element = "var e=angular.element(document.querySelector('[ng-app]'));"
    JsRaw(element+"e.scope().$apply(function(){e.injector().get('liftProxy').response("+stringify(p)+")});")
  }

  override def lowPriority = {
    case ReturnData(id, Full(data))         => callback(Resolve(id, data))
    case ReturnData(id, Empty)              => callback(Resolve(id, null))
    case ReturnData(id, Failure(msg, _, _)) => callback(Reject(id, msg))
  }

  sealed trait Promise{def id:String; def success:Boolean}
  case class Resolve(id:String, data:Any, success:Boolean = true) extends Promise
  case class Reject(id:String, msg:String, success:Boolean = false) extends Promise
}
