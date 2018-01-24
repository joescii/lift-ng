package net.liftmodules.ng

import scala.concurrent.{ExecutionContext, Future, Promise}
import net.liftweb.actor.LAFuture
import net.liftweb.common.{Box, Empty, Failure, Full}
import net.liftweb.json.{Formats, JValue}

import scala.util.{Success, Failure => SFailure}

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

    lazy val boxed: FutureBox[T] = {
      f.map(Box.legacyNullTest)
        .recover { case t: Throwable => Failure(t.getMessage, Full(t), Empty) }
    }
  }

  def LAFutureToFuture[T](f: LAFuture[Box[T]]): FutureBox[T] = {
      val p: Promise[Box[T]] = Promise()
      f.onComplete { dblBox => // Because we are an LAFuture[Box[T]], this yields Box[Box[T]]. Unfortunately flatten doesn't work here in Lift 2.6
        dblBox match {
          case Full(b) => p.success(b)
          case f: Failure => p.success(f)
          case Empty => p.success(Empty)
        }
      }
      p.future
    }

  implicit class EnhancedLAFuture[T](f: LAFuture[Box[T]]) {
    lazy val asScala: FutureBox[T] = LAFutureToFuture(f)
  }
}
