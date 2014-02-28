package net.liftmodules.ng
package test.comet

import net.liftweb._
import common._
import util._
import Helpers._
import java.util.concurrent.ScheduledFuture

class AsyncTestActor extends AngularActor with Loggable {
  self =>
  val iter = Stream.from(0).iterator

  def schedule():ScheduledFuture[Unit] = Schedule(() => {
    self ! "next"
  }, 1.second)

  def doUpdate() = {
    val next = iter.next.toString
    logger.info(s"calling scope.broadcast($next) from server.")
    broadcast("asyncMsg",next)
    schedule()
  }

  override def lowPriority = {
    case "start" => schedule()
    case "next" => doUpdate()
  }

  // Set a short lifespan so this kills off in a reasonable amount of time.
  override def lifespan = Full(10)
}
