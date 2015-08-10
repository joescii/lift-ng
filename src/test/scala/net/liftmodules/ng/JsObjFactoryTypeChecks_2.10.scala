package net.liftmodules.ng

import Angular._
import org.scalatest. { FlatSpec, ShouldMatchers }

class JsObjFactoryTypeChecks extends FlatSpec with ShouldMatchers {
  case class Model(str:String) extends NgModel
  case class NonModel(str:String)

  "jsObjFactory().jsonCall with an NgModel" should "compile" in {
    """ angular.module("module").factory("factory", jsObjFactory()
          .jsonCall("shouldCompile", (arg:Model) => net.liftweb.common.Full("String"))
        )
    """ should compile
  }

  "jsObjFactory().jsonCall with a Long" should "not type check" in {
    """ angular.module("module").factory("factory", jsObjFactory()
          .jsonCall("shouldNotTypeCheck", (arg:Long) => net.liftweb.common.Full("String"))
        )
    """ shouldNot typeCheck
  }

  "jsObjFactory().jsonCall with a case class not extending NgModel" should "not type check" in {
    """ angular.module("module").factory("factory", jsObjFactory()
          .jsonCall("shouldNotTypeCheck", (arg:NonModel) => net.liftweb.common.Full("String"))
        )
    """ shouldNot typeCheck
  }

  "jsObjFactory().defModelToAny with an NgModel" should "compile" in {
    """ angular.module("module").factory("factory", jsObjFactory()
          .defModelToAny("shouldCompile", (arg:Model) => net.liftweb.common.Full("String"))
        )
    """ should compile
  }

  "jsObjFactory().defModelToAny with a Long" should "not type check" in {
    """ angular.module("module").factory("factory", jsObjFactory()
          .defModelToAny("shouldNotTypeCheck", (arg:Long) => net.liftweb.common.Full("String"))
        )
    """ shouldNot typeCheck
  }

  "jsObjFactory().future with an NgModel" should "compile" in {
    """ angular.module("module").factory("factory", jsObjFactory()
          .future("shouldCompile", (arg:Model) => new net.liftweb.actor.LAFuture[net.liftweb.common.Box[String]])
        )
    """ should compile
  }

  // TODO: Can this be fixed? Not a big deal since it will be deprecated and replaced.
  "jsObjFactory().future with a Long" should "not type check" ignore {
    """ angular.module("module").factory("factory", jsObjFactory()
          .future("shouldNotTypeCheck", (arg:Long) => new net.liftweb.actor.LAFuture[net.liftweb.common.Box[String]])
        )
    """ shouldNot typeCheck
  }

  "jsObjFactory().defModelToFutureAny with an NgModel" should "compile" in {
    """ angular.module("module").factory("factory", jsObjFactory()
          .defModelToFutureAny("shouldCompile", (arg:Model) => new net.liftweb.actor.LAFuture[net.liftweb.common.Box[String]])
        )
    """ should compile
  }

  "jsObjFactory().defModelToFutureAny with a Long" should "not type check" in {
    """ angular.module("module").factory("factory", jsObjFactory()
          .defModelToFutureAny("shouldNotTypeCheck", (arg:Long) => new net.liftweb.actor.LAFuture[net.liftweb.common.Box[String]])
        )
    """ shouldNot typeCheck
  }
}
