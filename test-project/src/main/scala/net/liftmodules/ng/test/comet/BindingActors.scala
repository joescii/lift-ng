package net.liftmodules.ng
package test.comet

import net.liftmodules.ng.test.snippet.Server2ClientBindTests._
import net.liftmodules.ng.Angular.NgModel
import net.liftweb.http.S
import net.liftweb.common.Empty

case class Message(msg:String) extends NgModel

class CounterBindActor extends BindingActor[Counter] {
  override val bindTo = "count"
  override val initialValue = Counter(0)
}

class ArrayBindActor extends BindingActor[ListWrap[String]] {
  override val bindTo = "array"
  override val initialValue = ListWrap(List.empty[String])
}

class C2sBindActor extends BindingActor[Message] {
  override val bindTo = "input"
  override val initialValue = Message("")
  override def onClientUpdate(m:Message) = {
    S.session.map(_.sendCometActorMessage("C2sReturnActor", Empty, m))
    m
  }
}

class C2sReturnActor extends BindingActor[Message] {
  override val bindTo = "returned"
  override val initialValue = Message("server")
}