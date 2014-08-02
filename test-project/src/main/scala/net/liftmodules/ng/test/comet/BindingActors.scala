package net.liftmodules.ng
package test.comet

import net.liftmodules.ng.test.snippet.Server2ClientBindTests._
import net.liftmodules.ng.Angular.NgModel
import net.liftweb.http.S
import net.liftweb.common.Empty

case class Message(msg:String) extends NgModel

class CounterBindActor extends SimpleBindingActor[Counter] ("count", Counter(0))

class ArrayBindActor extends SimpleBindingActor[ListWrap[String]] ("array", ListWrap(List.empty[String]))

class C2sBindActor extends SimpleBindingActor[Message] ("input", Message(""), { m:Message =>
  S.session.map(_.sendCometActorMessage("C2sReturnActor", Empty, m))
  m
})

class C2sReturnActor extends SimpleBindingActor[Message] ("returned", Message("server"))
