package net.liftmodules.ng
package test.snippet

import Angular._

import scala.xml.NodeSeq

object StaticServices {
  def render(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("StaticServices").factory("staticService", jsObjFactory())
  )
}
