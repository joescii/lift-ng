package net.liftmodules.ng
package test.snippet

import Angular._
import net.liftweb.common.Empty
import net.liftweb.http.S
import net.liftweb.util.Schedule

object Server2ClientBindTests {
  def render = {
    var counting = false
    var count = 0

    val session = S.session.openOrThrowException("Piss off, Lou!")

    def schedule:Unit = Schedule(() => {
      if(counting) {
        session.sendCometActorMessage("CounterBindActor", Empty, count )
        count += 1
      }
      schedule
    }, 1000)

    schedule

    renderIfNotAlreadyDefined(angular.module("S2cBindServices").factory("counterService", jsObjFactory()
      .jsonCall("toggle", {
        counting = !counting
        Empty
      })
    ))
  }
}
