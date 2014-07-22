package net.liftmodules.ng

import Angular.NgModel
import net.liftweb.json.{JsonParser, DefaultFormats, parse}
import net.liftweb.json.Serialization._
import net.liftweb.json.JsonAST._
import net.liftweb.http.js.JE._
import js.JsonDeltaFuncs._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.SHtml._

abstract class BindingActor[M <: NgModel : Manifest] extends AngularActor {
  /** The client `\$scope` element to bind to */
  def bindTo:String

  case class ClientJson(json:String)

  var stateModel:Any = null
  var stateJson:JValue = JNull

  override def render = nodesToRender ++ Script(buildCmd(root = false,
    SetExp(JsVar("s()."+bindTo), stateJson) &
    Call("s().$watchCollection", JString(bindTo), AnonFunc("m",
      JsCrVar("u", Call("JSON.stringify", JsRaw("{add:m}"))) &
      ajaxCall(JsVar("u"), s => {
        this ! ClientJson(s)
        Noop
    })))
  ))

  def toJValue(m:Any):JValue = m match {
    case m:NgModel  => parse(write(m))
    case s:String   => JString(s)
    case i:Int      => JInt(i)
    case e => JNull
  }

  private implicit val formats = DefaultFormats
  override def lowPriority = {
    case m:NgModel  => fromServer(m)
    case s:String   => fromServer(s)
    case i:Int      => fromServer(i)
    case ClientJson(json) => fromClient(json)
    case e => logger.warn("Received un-handled model '"+e+"' of type '"+e.getClass.getName+"'.")
  }

  private def fromServer(m:Any) = {
    val mJs = toJValue(m)
    val diff = stateJson dfn mJs
    val cmd = buildCmd(root = false, diff(JsVar("s()."+bindTo)))
    partialUpdate(cmd)

    stateJson = mJs
    stateModel = m
  }

  private def fromClient(json:String) = {
//    implicit val formats = DefaultFormats
//    implicit val mf = manifest[String]

    val jUpdate = JsonParser.parse(json) \\ "add"
    logger.debug("From Client: "+jUpdate)
  }
}
