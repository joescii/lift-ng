package net.liftmodules.ng
package test.snippet

import Angular._
import net.liftweb.common.Empty

object Server2ClientBindTests {
  def render = renderIfNotAlreadyDefined(angular.module("S2cBindServices").factory("counterService", jsObjFactory()
    .jsonCall("startCounter", Empty)
  ))
}
