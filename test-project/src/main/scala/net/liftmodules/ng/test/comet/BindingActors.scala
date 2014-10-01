package net.liftmodules.ng
package test.comet

import net.liftmodules.ng.test.snippet.Server2ClientBindTests._
import net.liftmodules.ng.Angular.NgModel
import net.liftweb.http.S
import scala.util.Try

case class Message(msg:String) extends NgModel

class CounterBindingActor extends SimpleNgModelBinder[Counter] ("count", Counter(0)) with BindingToClient with SessionScope

class ArrayBindingActor extends SimpleNgModelBinder[ListWrap] ("array", ListWrap(List.empty[String])) with BindingToClient with SessionScope

class C2sBindingActor extends SimpleNgModelBinder[Message] ("input", Message(""), { m:Message =>
  for {
    session <- S.session
    comet <- session.findComet("C2sReturnActor")
  } { comet ! m }
  m
}) with BindingToServer with SessionScope

class C2sReturnActor extends SimpleNgModelBinder[Message] ("returned", Message("")) with BindingToClient with SessionScope

class Plus1BindingActor extends SimpleNgModelBinder[Message] ("counter", Message("0"))
  with BindingToClient with BindingToServer with SessionScope {
  override val onClientUpdate = { count:Message =>
    val next = Message(Try(count.msg.toInt + 1).getOrElse(-1).toString)
    this ! next
    next
  }
}