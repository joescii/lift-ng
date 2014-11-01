package net.liftmodules.ng
package test.snippet

import Angular._
import net.liftweb.actor.LAFuture
import net.liftmodules.ng.test.model.Test2Obj
import net.liftweb.common.{Full, Box, Empty}

case class EmbeddedFutures(
  resolved: LAFuture[Box[String]]
//  failed: LAFuture[String],
//  string: LAFuture[String],
//  obj: LAFuture[Test2Obj]
)

object EmbeddedFuturesSnips {
  def services = renderIfNotAlreadyDefined(
    angular.module("EmbeddedFutures")
      .factory("embeddedFutureServices", jsObjFactory()
        .future("fetch", {
          val f = new LAFuture[Box[EmbeddedFutures]]
          val resolved = new LAFuture[Box[String]]
          val model = EmbeddedFutures(resolved)
          f.satisfy(Full(model))
          resolved.satisfy(Full("resolved"))
          f
        })
      )
  )
}
