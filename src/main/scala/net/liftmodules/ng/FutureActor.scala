package net.liftmodules.ng
package comet

import Angular.ReturnData

class LiftNgFutureActor extends AngularActor {
  override def lowPriority = {
    case d:ReturnData => {
      println("emitting "+d)
      rootScope.emit("lift-ng-future", d)
    }
  }
}
