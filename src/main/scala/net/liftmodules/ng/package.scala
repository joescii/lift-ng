package net.liftmodules

import net.liftweb.common.{Empty, Failure, Full, Loggable}
import net.liftweb.util.StringHelpers._

package object ng extends Loggable {
  private [ng] def throwableToFailure(t: Throwable): Failure =  {
    logger.warn("Uncaught exception while processing ajax function", t)
    val msg = encJs(t.getMessage).drop(1).dropRight(1) // Encode into valid JS, but strip the quotes it adds
    Failure(msg, Full(t), Empty)
  }

}
