package net.liftmodules.ng
package test.snippet

import test.model._
import Angular._
import net.liftweb.actor.LAFuture
import net.liftweb.common._
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq

object FailureSnips {
  case class TestException(msg: String) extends Exception

  def failure(msg: String): Failure = Failure("Wrong string", Full(new TestException(msg)), Empty)

  def future(msg: String): LAFuture[Box[String]] = {
    val f = new LAFuture[Box[String]]()
    Schedule.schedule(() => f.satisfy(failure(msg)), 1 second)
    f
  }

  def services(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("FailureHandler").factory("failureServices", jsObjFactory()

      .defAny("defAny_failure", failure("defAny_failure test"))

      .defStringToAny("defStringToAny_failure", _ => failure("defStringToAny_failure test"))

      .defModelToAny("defModelToAny_failure", (_: Test2Obj) => failure("defModelToAny_failure"))

      .defFutureAny("defFutureAny_failure", future("defFutureAny_failure"))

      .defStringToFutureAny("defStringToFutureAny_failure", _ => future("defStringToFutureAny_failure"))

      .defModelToFutureAny("defModelToFutureAny_failure", (_: Test2Obj) => future("defModelToFutureAny_failure"))

//      .defAny("defAny_exception", ???)
//      .defStringToAny("defStringToAny_exception", ???)
//      .defModelToAny("defModelToAny_exception", ???)
//
//      .defFutureAny("defFutureAny_inner_exception", ???)
//      .defStringToFutureAny("defStringToFutureAny_inner_exception", ???)
//      .defModelToFutureAny("defModelToFutureAny_inner_exception", ???)
//
//      .defFutureAny("defFutureAny_outer_exception", ???)
//      .defStringToFutureAny("defStringToFutureAny_outer_exception", ???)
//      .defModelToFutureAny("defModelToFutureAny_outer_exception", ???)

    )
  )
}
