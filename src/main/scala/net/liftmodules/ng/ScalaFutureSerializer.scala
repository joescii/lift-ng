package net.liftmodules.ng

import net.liftmodules.ng.Angular.{handleFailure, plumbFuture, rand}
import net.liftweb.json.{Formats, JField, JObject, JString}
import net.liftweb.json.JsonAST.JValue

import scala.concurrent.Future
import FutureConversions._

import scala.util.{Failure, Success}

trait ScalaFutureSerializer {
  def future2JValue[T](formats: Formats, future: FutureBox[T])(implicit ec: ExecutionContextProvider) = {
    implicit val f = formats + new LAFutureSerializer

    val id = rand
    val idField = JField("net.liftmodules.ng.Angular.future", JString(id))
    
    def resolveLater: JObject = {
      plumbFuture(future, id)

      JObject(List(
        JField("state", JString("pending"))
      ))
    }

    val data = if(future.isCompleted) {
      val json = future.value match {
        case Some(Success(box)) => promiseToJson(Angular.DefaultApiSuccessMapper.boxToPromise(box))
        case Some(Failure(exception)) => promiseToJson(handleFailure(throwableToFailure(exception)))
        case _ => resolveLater
      }

      json
    } else {
      resolveLater
    }

    JObject(List(idField)) merge data
  }

  def scalaFutureSerializer(formats: Formats)(implicit ec: ExecutionContextProvider): PartialFunction[Any, JValue] = {
    case future: Future[_] => future2JValue(formats, boxed(future))
  }
}

