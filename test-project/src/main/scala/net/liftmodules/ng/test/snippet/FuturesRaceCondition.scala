package net.liftmodules.ng.test.snippet

import net.liftmodules.ng.Angular._
import net.liftweb.common.Full
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

case class FuturesRaceConditionModel(
  f0: Future[String],
  f1: Future[String],
  f2: Future[String],
  f3: Future[String],
  f4: Future[String],
  f5: Future[String],
  f6: Future[String],
  f7: Future[String],
  f8: Future[String],
  f9: Future[String],
  f10: Future[String],
  f11: Future[String],
  f12: Future[String],
  f13: Future[String],
  f14: Future[String],
  f15: Future[String],
  f16: Future[String],
  f17: Future[String],
  f18: Future[String],
  f19: Future[String],
  f20: Future[String]
) extends NgModel

object FuturesRaceCondition {
  def services = renderIfNotAlreadyDefined(
    angular.module("FuturesRaceCondition")
      .factory("futuresRaceConditionServices", jsObjFactory()
      .jsonCall("fetch", { Full(newModel) })
      )
    )

  def newModel = FuturesRaceConditionModel(
    future("0"),
    future("1"),
    future("2"),
    future("3"),
    future("4"),
    future("5"),
    future("6"),
    future("7"),
    future("8"),
    future("9"),
    future("10"),
    future("11"),
    future("12"),
    future("13"),
    future("14"),
    future("15"),
    future("16"),
    future("17"),
    future("18"),
    future("19"),
    future("20")
  )
}
