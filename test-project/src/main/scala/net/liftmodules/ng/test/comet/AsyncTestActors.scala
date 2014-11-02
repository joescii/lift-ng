package net.liftmodules.ng
package test.comet

import test.model.BroadcastObj

import net.liftweb._
import common._
import util._
import Helpers._
import java.util.concurrent.ScheduledFuture
import net.liftmodules.ng.test.snippet.EmbeddedFuturesSnips

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

class RootScopeBroadcastStringActor extends AsyncTestActor {
  val iter = Stream.from(0).iterator

  override def doUpdate() = {
    val next = iter.next.toString
    logger.debug(s"calling rootScope.broadcast($next) from server.")
    rootScope.broadcast("rootScopeBroadcastString",next)
  }
}

class RootScopeBroadcastJsonActor extends AsyncTestActor {
  val nums  = Stream.from(0).iterator
  val chars = Stream.from(97).map(_.asInstanceOf[Char]).iterator

  override def doUpdate() = {
    val next = BroadcastObj(nums.next, chars.next.toString)
    logger.debug(s"calling rootScope.broadcast($next) from server.")
    rootScope.broadcast("rootScopeBroadcastJson",next)
  }
}

class RootScopeEmitStringActor extends AsyncTestActor {
  val iter = Stream.from(0).iterator

  override def doUpdate() = {
    val next = iter.next.toString
    logger.debug(s"calling rootScope.emit($next) from server.")
    rootScope.emit("rootScopeEmitString",next)
  }
}

class RootScopeEmitJsonActor extends AsyncTestActor {
  val nums  = Stream.from(0).iterator
  val chars = Stream.from(97).map(_.asInstanceOf[Char]).iterator

  override def doUpdate() = {
    val next = BroadcastObj(nums.next, chars.next.toString)
    logger.debug(s"calling rootScope.emit($next) from server.")
    rootScope.emit("rootScopeEmitJson",next)
  }
}

class EarlyEmitActor extends AngularActor { self =>
  val nums  = Stream.from(0).iterator

  override def lowPriority = {
    case "go" => go
    case i:Int => println("emitting"); rootScope.emit("earlyEmit", i.toString)
  }

  self ! nums.next()

  def go = for(t <- 500 to 1500 by 500) {
    Schedule(() => {self ! nums.next()}, t.millis)
  }

  go
}

class ScopeActor extends AngularActor {
  override def lowPriority = {
    case "emit" => scope.emit("scope-msg", "emit")
    case "broadcast" => scope.broadcast("scope-msg", "broadcast")
  }
}

class AssignmentActor extends AngularActor {
  override def lowPriority = {
    case "start" => {
      rootScope.assign("rootStr", "Root String")
      rootScope.assign("rootObj", BroadcastObj(1, "a"))
      scope.assign("scopeStr", "Scope String")
      scope.assign("scopeObj", BroadcastObj(2, "b"))
    }

  }
}

class EmbeddedFutureActor extends AngularActor {
  override def lowPriority = {
    case "go" => {
      EmbeddedFuturesSnips.buildFuture.foreach(_.foreach(scope.emit("embedded", _)))
    }
  }
}