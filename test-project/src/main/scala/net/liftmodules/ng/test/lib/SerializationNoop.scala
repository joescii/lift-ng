package net.liftmodules.ng.test.lib

import net.liftweb.http.LiftSession

object LiftSessionSerialization {
  def roundTrip(s: LiftSession): LiftSession = {
    s
  }
}
