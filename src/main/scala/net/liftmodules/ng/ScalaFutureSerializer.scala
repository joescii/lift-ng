package net.liftmodules.ng

import net.liftmodules.ng.Angular.{plumbFuture, rand}
import net.liftweb.json.{Formats, JField, JObject, JString}
import net.liftweb.json.JsonAST.JValue

import scala.concurrent.Future

import FutureConversions._

trait ScalaFutureSerializer {
  def future2JValue[T](formats: Formats, future: FutureBox[T])(implicit ec: ExecutionContextProvider) = {
    implicit val f = formats + new LAFutureSerializer

    val id = rand
    plumbFuture(future, id)

    JObject(List(
      JField("state", JString("pending")),
      JField("net.liftmodules.ng.Angular.future", JString(id))
    ))
  }

  def scalaFutureSerializer(formats: Formats)(implicit ec: ExecutionContextProvider): PartialFunction[Any, JValue] = {
    case future: Future[_] => future2JValue(formats, boxed(future))
  }
}

