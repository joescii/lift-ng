package net.liftmodules.ng
package test.snippet

import Angular._
import net.liftweb.common.Empty
import net.liftweb.http.S
import net.liftweb.util.Schedule

object Server2ClientBindTests {
  case class ListWrap[A](l:List[A] = List.empty[A]) extends NgModel {
    def :+ (a:A) = ListWrap(l :+ a)
  }
  case class Counter(current:Int) extends NgModel

  def render = {
    var counting = false
    var count = 0

    val session = S.session.openOrThrowException("Piss off, Lou!")

    def schedule:Unit = Schedule(() => {
      if(counting) {
        session.findComet("CounterBindActor").foreach( _ ! Counter(count) )
        count += 1
      }
      schedule
    }, 1000)

    schedule

    var array = ListWrap[String]()

    renderIfNotAlreadyDefined(angular.module("S2cBindServices").factory("counterService", jsObjFactory()
      .jsonCall("toggle", {
        counting = !counting
        Empty
      })
    ).factory("arrSvc", jsObjFactory()
      .jsonCall("next", {
        array = array :+ (new java.util.Date().toString)
        session.findComet("ArrayBindActor").foreach( _ ! array )
        Empty
      })
    ))
  }
}
