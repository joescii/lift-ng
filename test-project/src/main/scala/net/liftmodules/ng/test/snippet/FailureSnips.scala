package net.liftmodules.ng
package test.snippet

import test.model._
import Angular._
import net.liftweb.actor.LAFuture
import net.liftweb.common._
import net.liftweb.json.DefaultFormats
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._

import scala.concurrent.Future
import scala.xml.NodeSeq
import scala.concurrent.ExecutionContext.Implicits.global

object FailureSnips {
  implicit val formats = DefaultFormats
  case class TestException(msg: String) extends Exception

  def failure(msg: String): Failure = Failure("Wrong string", Full(new TestException(msg)), Empty)

  def future(msg: String): Future[String] = {
    val p = scala.concurrent.Promise[String]()

    Schedule.schedule(() => p.failure(new Exception(msg)), 1 second)
    p.future
  }

  def except(msg: String): Nothing = throw new TestException(msg)

  // TODO after dropping pre-2.10 support...
//  def exceptedFuture(msg: String): Future[String] = Future(except(msg))

  def services(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("FailureHandler").factory("failureServices", jsObjFactory()

      .defAny("defAny_failure", failure("defAny_failure test"))

      .defParamToAny("defStringToAny_failure", (_: String) => failure("defStringToAny_failure test"))

      .defModelToAny("defModelToAny_failure", (_: Test2Obj) => failure("defModelToAny_failure test"))

      .defFutureAny("defFutureAny_failure", future("defFutureAny_failure test"))

      .defParamToFutureAny("defStringToFutureAny_failure", (_: String) => future("defStringToFutureAny_failure test"))

      .defParamToFutureAny("defModelToFutureAny_failure", (_: Test2Obj) => future("defModelToFutureAny_failure test"))

      .defAny("defAny_exception", except("defAny_exception test"))

      .defStringToAny("defStringToAny_exception", _ => except("defStringToAny_exception test"))

      .defModelToAny("defModelToAny_exception", (_: Test2Obj) => except("defModelToAny_exception test"))

      .defFutureAny("defFutureAny_outer_exception", except("defFutureAny_outer_exception test").asInstanceOf[Future[Any]])

      .defStringToFutureAny("defStringToFutureAny_outer_exception", (_: String) => except("defStringToFutureAny_outer_exception test"))

      .defModelToFutureAny("defModelToFutureAny_outer_exception", (_: Test2Obj) => except("defModelToFutureAny_outer_exception test"))

//      .defFutureAny("defFutureAny_inner_exception", ???)
//      .defStringToFutureAny("defStringToFutureAny_inner_exception", ???)
//      .defModelToFutureAny("defModelToFutureAny_inner_exception", ???)
    )
  )
}
