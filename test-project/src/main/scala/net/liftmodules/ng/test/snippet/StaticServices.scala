package net.liftmodules.ng
package test.snippet

import Angular._
import test.model.StringInt

import scala.xml.NodeSeq

object StaticServices {
  def render(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("StaticServices").factory("staticService", jsObjFactory()
      .vals(
        string = "FromServer1",
        integer = 42,
        obj = StringInt("FromServer2", 88)
      )
    )
  )
}
