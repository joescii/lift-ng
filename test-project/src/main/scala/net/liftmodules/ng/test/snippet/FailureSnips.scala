package net.liftmodules.ng
package test.snippet

import test.model._
import Angular._
import net.liftweb.common._
import net.liftweb.json.DefaultFormats
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._
import test.lib.SerializableECP._

import scala.concurrent.Future
import scala.xml.NodeSeq

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

  def exceptedFuture(msg: String): Future[String] = Future(except(msg))

  def services(xhtml:NodeSeq) = renderIfNotAlreadyDefined(
    angular.module("FailureHandler").factory("failureServices", jsObjFactory()

      .defAny("defAny_failure", failure("defAny_failure test"))

      .defParamToAny("defParamToAny_failure", (_: Test2Obj) => failure("defParamToAny_failure test"))

      .defFutureAny("defFutureAny_failure", future("defFutureAny_failure test"))

      .defParamToFutureAny("defParamToFutureAny_failure", (_: Test2Obj) => future("defParamToFutureAny_failure test"))

      .defAny("defAny_exception", except("defAny_exception test"))

      .defParamToAny("defParamToAny_exception", (_: Test2Obj) => except("defParamToAny_exception test"))

      .defFutureAny("defFutureAny_outer_exception", except("defFutureAny_outer_exception test").asInstanceOf[Future[Any]])

      .defParamToFutureAny("defParamToFutureAny_outer_exception", (_: Test2Obj) => except("defParamToFutureAny_outer_exception test"))

      .defFutureAny("defFutureAny_inner_exception", exceptedFuture("defFutureAny_inner_exception test"))

      .defParamToFutureAny("defParamToFutureAny_inner_exception", (_: Test2Obj) => exceptedFuture("defParamToFutureAny_inner_exception test"))
    )
  )
}
