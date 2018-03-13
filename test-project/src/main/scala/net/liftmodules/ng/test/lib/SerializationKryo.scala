package net.liftmodules.ng.test.lib

import net.liftmodules.cluster.kryo.KryoSerializable
import net.liftweb.http.LiftSession

object LiftSessionSerialization {
  def roundTrip(s: LiftSession): LiftSession = KryoSerializable.roundTrip(s)
}
