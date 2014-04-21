package net.liftmodules.ng
package comet

import Angular.ReturnData
import net.liftweb.common._

class LiftNgFutureActor extends AngularActor {
  override def lowPriority = {
    case ReturnData(id, Full(data))         => rootScope.emit("lift-ng-future", Resolve(id, data))
    case ReturnData(id, Empty)              => rootScope.emit("lift-ng-future", Resolve(id, null))
    case ReturnData(id, Failure(msg, _, _)) => rootScope.emit("lift-ng-future", Reject(id, msg))
  }

  sealed trait Promise{def id:String; def success:Boolean}
  case class Resolve(id:String, data:Any, success:Boolean = true) extends Promise
  case class Reject(id:String, msg:String, success:Boolean = false) extends Promise
}
