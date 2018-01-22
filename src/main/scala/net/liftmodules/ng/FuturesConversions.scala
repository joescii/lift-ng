package net.liftmodules.ng

import net.liftweb.json._

import scala.concurrent.{ExecutionContext, Future}
import net.liftweb.actor.LAFuture
import net.liftweb.common.{Box, Empty, Failure, Full}

import scala.util.{ Success, Failure => SFailure }

object AngularExecutionContext {
  implicit var ec: ExecutionContext = ExecutionContext.global
  def apply(ec: ExecutionContext) {
    this.ec = ec
  }
}

trait ScalaFutureSerializer {
  def scalaFutureSerializer(formats: Formats)(implicit ec: ExecutionContext): PartialFunction[Any, JValue] = {
    case future: Future[_] => LAFutureSerializer.laFuture2JValue(formats, FutureConversions.FutureToLAFuture(future))
  }
}

object FutureConversions {
  implicit def FutureToLAFuture[T](f: Future[T])(implicit ec: ExecutionContext):LAFuture[Box[T]] = f.la

  implicit class ConvertToLA[T](f: Future[T])(implicit ec: ExecutionContext) {
    lazy val la: LAFuture[Box[T]] = {
      val laf = new LAFuture[Box[T]]()

      f.onComplete {
        case Success(t) => laf.satisfy(Box.legacyNullTest(t))
        case SFailure(ex) => laf.satisfy(throwableToFailure(ex))
      }

      laf
    }

    lazy val boxed: Future[Box[T]] =
      f.map(Box.legacyNullTest)
      .recover { case t: Throwable => Failure(t.getMessage, Full(t), Empty) }
  }

  implicit class EnhancedFutureOfBox[T](f: Future[Box[T]])(implicit ec: ExecutionContext) {
    lazy val boxed: Future[Box[T]] =
      f.map(b => Box.legacyNullTest(b).flatten)
      .recover { case t: Throwable => Failure(t.getMessage, Full(t), Empty) }
  }
}
