package net.liftmodules.ng.test.snippet

import net.liftmodules.ng.test.BuildInfo._

import net.liftweb.util.Helpers._

object AppSnips {
  def title = ".alt *" #> s"($liftVersion/$scalaVersion)"
}
