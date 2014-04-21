package net.liftmodules.ng
package comet

import Angular.ReturnData
import net.liftweb.common._

class LiftNgFutureActor extends AngularActor {
  override def lowPriority = {
    case ReturnData(id, Full(data))         => rootScope.emit("lift-ng-future", Resolve(data))
    case ReturnData(id, Empty)              => rootScope.emit("lift-ng-future", Resolve(null))
    case ReturnData(id, Failure(msg, _, _)) => rootScope.emit("lift-ng-future", Reject(msg))
  }

  sealed trait Promise{def success:Boolean}
  case class Resolve(data:Any, success:Boolean = true) extends Promise
  case class Reject(reason:String, success:Boolean = false) extends Promise
}
