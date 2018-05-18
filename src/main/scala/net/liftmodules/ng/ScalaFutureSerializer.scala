package net.liftmodules.ng

import net.liftmodules.ng.Angular.{plumbFuture, rand}
import net.liftweb.json.{Formats, JField, JObject, JString}
import net.liftweb.json.JsonAST.JValue

import scala.concurrent.Future
import FutureConversions._

import scala.util.Success

trait ScalaFutureSerializer {
  def future2JValue[T](formats: Formats, future: FutureBox[T])(implicit ec: ExecutionContextProvider) = {
    implicit val f = formats + new LAFutureSerializer

    val id = rand
    val idField = JField("net.liftmodules.ng.Angular.future", JString(id))

    val data = if(future.isCompleted) {
      future.value match {
        case Some(Success(box)) =>
          val promise = Angular.DefaultApiSuccessMapper.boxToPromise(box)
          val json = promiseToJson(promise)
          json
      }
    } else {
      plumbFuture(future, id)

      JObject(List(
        JField("state", JString("pending"))
      ))
    }

    JObject(List(idField)) merge data
  }

  def scalaFutureSerializer(formats: Formats)(implicit ec: ExecutionContextProvider): PartialFunction[Any, JValue] = {
    case future: Future[_] => future2JValue(formats, boxed(future))
  }
}

