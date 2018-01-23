package net.liftmodules.ng
package test.snippet

import Angular._
import net.liftweb.json.DefaultFormats
import test.model.StringInt

import scala.xml.NodeSeq

object StaticServices {
  implicit val formats = DefaultFormats

  def render(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("StaticServices").factory("staticService", jsObjFactory()
      .string("string", "FromServer1")
      .valAny("integer", 42)
      .json("obj", StringInt("FromServer2", 88))
    )
  )
}
