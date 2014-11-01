package net.liftmodules.ng
package test.snippet

import Angular._
import net.liftweb.actor.LAFuture
import net.liftmodules.ng.test.model.Test2Obj
import net.liftweb.common.{Failure, Full, Box, Empty}
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._

case class EmbeddedFutures(
  resolved: LAFuture[Box[String]],
  failed: LAFuture[Box[String]]
//  string: LAFuture[Box[String]],
//  obj: LAFuture[Box[Test2Obj]]
)

object EmbeddedFuturesSnips {
  def services = renderIfNotAlreadyDefined(
    angular.module("EmbeddedFutures")
      .factory("embeddedFutureServices", jsObjFactory()
        .future("fetch", {
          val f = new LAFuture[Box[EmbeddedFutures]]

          val resolved = new LAFuture[Box[String]]
          resolved.satisfy(Full("resolved"))

          val failed = new LAFuture[Box[String]]
          satisfy(failed, Failure("failed"))

          val model = EmbeddedFutures(resolved, failed)
          f.satisfy(Full(model))
          f
        })
      )
  )

  def satisfy[T](future:LAFuture[Box[T]], value:Box[T]) {
    def delay = (Math.random() * 2000).toInt.millis
    Schedule(() => {
      future.satisfy(value)
    }, delay)
  }
}
