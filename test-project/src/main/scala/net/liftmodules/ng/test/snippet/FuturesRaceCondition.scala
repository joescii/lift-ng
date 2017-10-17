package net.liftmodules.ng.test.snippet

import net.liftmodules.ng.Angular._
import net.liftweb.common.Full
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

case class FuturesRaceConditionModel(
  fs: List[Future[String]],
  garbage: List[String]
)

object FuturesRaceCondition {
  def services = renderIfNotAlreadyDefined(
    angular.module("FuturesRaceCondition")
      .factory("futuresRaceConditionServices", jsObjFactory()
      .jsonCall("fetch", { Full(newModel) })
      )
    )

  def fib(n:Int):Int = if(n <= 2) 1 else fib(n - 1) + fib(n - 2)

  def newModel = {
    val garbage = (1 to 100000).map(i => i.toString).toList
    val fs = (1 to 10).map(i => Future(i.toString)).toList
    FuturesRaceConditionModel(fs, garbage)
  }

  def divs = (0 to 9).map(i => <div id={s"f$i"} ng-bind={s"obj[$i]"}>Hard-coded</div>)
}
