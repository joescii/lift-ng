package net.liftmodules.ng

import Angular._
import FutureConversions._
import net.liftweb._
import json._
import actor.LAFuture
import common._

import scala.concurrent.ExecutionContext

object LAFutureSerializer {
  def laFuture2JValue[T](formats: Formats, future: LAFuture[Box[T]])(implicit ec: ExecutionContext) = {
    implicit val f = formats + new LAFutureSerializer

    val id = rand
    val flagField = JField("net.liftmodules.ng.Angular.future", JString(id))
    val valObj: JObject =
      if (!future.isSatisfied) {
        plumbFuture(future.asScala, id)
        JObject(List(JField("state", JString("pending"))))
      } else {
        val box = future.get
        val promise = Angular.DefaultApiSuccessMapper.boxToPromise(box)
        val json = promiseToJson(promise)
        json
      }

    JObject(List(flagField)) merge valObj
  }

  def laFutureSerializer(formats: Formats, ec: ExecutionContext): PartialFunction[Any, JValue] = {
    case future: LAFuture[_] => laFuture2JValue(formats, future.asInstanceOf[LAFuture[Box[Any]]])(ec)
  }

}
class LAFutureSerializer[T : Manifest] extends Serializer[LAFuture[Box[T]]] with ScalaFutureSerializer {
  import LAFutureSerializer._
  import AngularExecutionContext._

  private def deserializer:PartialFunction[JValue, LAFuture[Box[T]]] = {
    case JObject(List(JField("net.liftmodules.ng.Angular.future", JString(id)))) => {
      val future = new LAFuture[Box[T]]()
      future.abort()
      future
    }
  }

  // The stuff below was copy/pasted from CustomSerializer.  Because we need to recursively call ourselves,
  // it's not possible to use CustomSerializer.
  val Class = implicitly[Manifest[LAFuture[Box[T]]]].runtimeClass

  def deserialize(implicit format: Formats) = {
    case (TypeInfo(Class, _), json) =>
      if (deserializer.isDefinedAt(json)) deserializer(json)
      else throw new MappingException("Can't convert " + json + " to " + Class)
  }

  def serialize(implicit format: Formats) = laFutureSerializer(format, ec) orElse scalaFutureSerializer(format)
}