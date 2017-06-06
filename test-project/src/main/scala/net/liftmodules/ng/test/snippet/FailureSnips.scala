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

  def services(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("FailureHandler").factory("failureServices", jsObjFactory()
//      .defAny("defAny_failure", ???)
//      .defStringToAny("defStringToAny_failure", ???)
//      .defModelToAny("defModelToAny_failure", ???)
      .defFutureAny("defFutureAny_failure", {
        val f = new LAFuture[Box[String]]()
        Schedule.schedule(() => f.satisfy(failure("defFutureAny_failure test")), 1 second)
        f
      })
//      .defStringToFutureAny("defStringToFutureAny_failure", ???)
//      .defModelToFutureAny("defModelToFutureAny_failure", ???)
//
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
