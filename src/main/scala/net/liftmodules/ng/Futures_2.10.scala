package net.liftmodules.ng

import net.liftweb.json._
import scala.concurrent.{ExecutionContext, Future}
import net.liftweb.actor.LAFuture
import net.liftweb.common.{Empty, Failure, Full, Box}
import net.liftmodules.ng.Angular.NgModel

object AngularExecutionContext {
  implicit var ec:ExecutionContext = ExecutionContext.global
  def apply(ec:ExecutionContext) {
    this.ec = ec
  }
}

object FutureConversions {
  implicit def FutureToLAFuture[T](f:Future[T])(implicit ctx:ExecutionContext):LAFuture[Box[T]] = f.la

  implicit class ConvertToLA[T](f: Future[T])(implicit ctx:ExecutionContext) {
    lazy val la:LAFuture[Box[T]] = {
      val laf = new LAFuture[Box[T]]()
      f.foreach(t => laf.satisfy(Full(t)))
      f.onFailure({ case t:Throwable => laf.satisfy(Failure(t.getMessage, Full(t), Empty)) })
      laf
    }
  }
}

class LAFutureSerializer[T <: NgModel](implicit ctx:ExecutionContext, m: Manifest[T]) extends LAFutureSerializerBase[T] {
  override val futureSerializer = new LAFutureSerializer[T]

  private def scalaFutureSerializer(formats:Formats)(implicit ctx:ExecutionContext):PartialFunction[Any, JValue] = {
    case future:Future[_] => laFuture2JValue(formats, FutureConversions.FutureToLAFuture(future))
  }

  def serialize(implicit format: Formats) = laFutureSerializer(format) orElse scalaFutureSerializer(format)
}