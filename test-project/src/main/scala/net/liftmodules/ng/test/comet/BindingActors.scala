package net.liftmodules.ng
package test.comet

import net.liftmodules.ng.test.snippet.Server2ClientBindTests._
import net.liftmodules.ng.Angular.NgModel
import net.liftweb.http.S
import net.liftweb.common.Empty

case class Message(msg:String) extends NgModel

class CounterBindActor extends SimpleBindingActor[Counter] ("count", Counter(0))

class ArrayBindActor extends SimpleBindingActor[ListWrap] ("array", ListWrap(List.empty[String]))

class C2sBindActor extends SimpleBindingActor[Message] ("input", Message(""), { m:Message =>
  for {
    session <- S.session
    comet <- session.findComet("C2sReturnActor")
  } { comet ! m }
  m
})

class C2sReturnActor extends SimpleBindingActor[Message] ("returned", Message(""))
