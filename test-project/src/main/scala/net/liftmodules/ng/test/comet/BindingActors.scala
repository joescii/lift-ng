package net.liftmodules.ng
package test.comet

import net.liftmodules.ng.test.snippet.Server2ClientBindTests._
import net.liftmodules.ng.Angular.NgModel
import net.liftweb.http.S
import scala.util.Try

case class Message(msg:String) extends NgModel

class CounterBindActor extends SimpleNgModelBinder[Counter] ("count", Counter(0)) with BindToClient with BindToSession

class ArrayBindActor extends SimpleNgModelBinder[ListWrap] ("array", ListWrap(List.empty[String])) with BindToClient with BindToSession

class C2sBindActor extends SimpleNgModelBinder[Message] ("input", Message(""), { m:Message =>
  for {
    session <- S.session
    comet <- session.findComet("C2sReturnActor")
  } { comet ! m }
  m
}) with BindToServer with BindToSession

class C2sReturnActor extends SimpleNgModelBinder[Message] ("returned", Message("")) with BindToClient with BindToSession

class Plus1BindActor extends SimpleNgModelBinder[Message] ("counter", Message("0"))
  with BindToClient with BindToServer with BindToSession {
  override val onClientUpdate = { count:Message =>
    val next = Message(Try(count.msg.toInt + 1).getOrElse(-1).toString)
    this ! next
    next
  }
}