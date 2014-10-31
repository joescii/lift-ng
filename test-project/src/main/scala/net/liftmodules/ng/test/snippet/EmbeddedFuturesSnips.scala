package net.liftmodules.ng
package test.snippet

import Angular._

object EmbeddedFuturesSnips {
  def services = renderIfNotAlreadyDefined(
    angular.module("EmbeddedFutures")
      .factory("embeddedFutureServices", jsObjFactory()
        // TODO
      )
  )
}
