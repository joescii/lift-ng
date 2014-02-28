package net.liftmodules.ng
package test.comet

import test.model.BroadcastObj

import net.liftweb._
import common._
import util._
import Helpers._
import java.util.concurrent.ScheduledFuture

abstract class AsyncTestActor extends AngularActor with Loggable {
  self =>

  def schedule():ScheduledFuture[Unit] = Schedule(() => {
    self ! "next"
  }, 1.second)

  def doUpdate():Unit

  private def doUpdatePrivate() = {
    doUpdate()
    schedule()
  }

  override def lowPriority = {
    case "start" => schedule()
    case "next" => doUpdatePrivate()
  }

  // Set a short lifespan so this kills off in a reasonable amount of time.
  override def lifespan = Full(10)
}

class BroadcastStringActor extends AsyncTestActor {
  val iter = Stream.from(0).iterator

  override def doUpdate() = {
    val next = iter.next.toString
    logger.info(s"calling scope.broadcast($next) from server.")
    scope.broadcast("broadcastString",next)
  }
}

class BroadcastJsonActor extends AsyncTestActor {
  val nums  = Stream.from(0).iterator
  val chars = Stream.from(97).map(_.asInstanceOf[Char]).iterator

  override def doUpdate() = {
    val next = BroadcastObj(nums.next, chars.next.toString)
    logger.info(s"calling scope.broadcast($next) from server.")
    scope.broadcast("broadcastJson",next)
  }
}