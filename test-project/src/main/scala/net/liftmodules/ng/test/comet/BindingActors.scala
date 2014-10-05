package net.liftmodules.ng
package test.comet

import net.liftmodules.ng.test.snippet.Server2ClientBindTests._
import net.liftmodules.ng.Angular.NgModel
import net.liftweb.http.S
import scala.util.Try
import net.liftweb.util.Schedule

case class Message(msg:String) extends NgModel

class CounterSessionBindActor extends SimpleNgModelBinder[Counter] ("count", Counter(0)) with BindingToClient with SessionScope

class ArraySessionBindActor extends SimpleNgModelBinder[ListWrap] ("array", ListWrap(List.empty[String])) with BindingToClient with SessionScope

class C2sSessionBindActor extends SimpleNgModelBinder[Message] ("input", Message(""), { m:Message =>
  for {
    session <- S.session
    comet <- session.findComet("C2sSessionReturnActor")
  } { comet ! m }
  m
}) with BindingToServer with SessionScope

class C2sSessionReturnActor extends SimpleNgModelBinder[Message] ("returned", Message("")) with BindingToClient with SessionScope

class Plus1SessionBindActor extends SimpleNgModelBinder[Message] ("counter", Message("0"))
  with BindingToClient with BindingToServer with SessionScope {
  override val onClientUpdate = { count:Message =>
    val next = Message(Try(count.msg.toInt + 1).getOrElse(-1).toString)
    this ! next
    next
  }
}


class CounterRequestBindActor extends SimpleNgModelBinder[Counter] ("count", Counter(0)) with BindingToClient {
  var count = 0

  def schedule:Unit = Schedule(() => {
    this ! Counter(count)
    count += 1
    schedule
  }, 1000)

  schedule

}

class ArrayRequestBindActor extends SimpleNgModelBinder[ListWrap] ("array", ListWrap(List.empty[String])) with BindingToClient

class C2sRequestBindActor extends SimpleNgModelBinder[Message] ("input", Message(""), { m:Message =>
  for {
    session <- S.session
    comet <- session.findComet("C2sSessionReturnActor")
  } { comet ! m }
  m
}) with BindingToServer

class Plus1RequestBindActor extends SimpleNgModelBinder[Message] ("counter", Message("0"))
with BindingToClient with BindingToServer {
  override val onClientUpdate = { count:Message =>
    val next = Message(Try(count.msg.toInt + 1).getOrElse(-1).toString)
    this ! next
    next
  }
}