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
  def initialValue:M

  private val lastServerVal = "net_liftmodules_ng_"

  def onClientUpdate(m:M):M = m

  case class ClientJson(json:String)

  var stateModel:M = initialValue
  var stateJson:JValue = JNull

  override def render = nodesToRender ++ Script(buildCmd(root = false,
    SetExp(JsVar("s()."+bindTo), stateJson) & // Send the current state with the page
    SetExp(JsVar("s()."+lastServerVal+bindTo), JsVar("s()."+bindTo)) & // Set the last server val to avoid echoing it back
    Call("s().$watchCollection", JString(bindTo), AnonFunc("n,o",
      // If the new value, n, is not equal to the last server val, send it.
      JsIf(JsNotEq(JsVar("n"), JsRaw("s()."+lastServerVal+bindTo)),
        JsCrVar("u", Call("JSON.stringify", JsRaw("{add:n}"))) &
        ajaxCall(JsVar("u"), s => {
          this ! ClientJson(s)
          Noop
      }), // else remove our last saved value so we can forget about it
        SetExp(JsVar("s()."+lastServerVal+bindTo), JsNull)
    )))
  ))

  def toJValue(m:Any):JValue = m match {
    case m:NgModel  => parse(write(m))
    case e => JNull
  }

  private implicit val formats = DefaultFormats
  override def lowPriority = {
    case m:M => fromServer(m)
    case ClientJson(json) => fromClient(json)
    case e => logger.warn("Received un-handled model '"+e+"' of type '"+e.getClass.getName+"'.")
  }

  private def fromServer(m:M) = {
    val mJs = toJValue(m)
    val diff = stateJson dfn mJs
    val cmd = buildCmd(root = false, diff(JsVar("s()."+bindTo)) & // Send the diff
      SetExp(JsVar("s()."+lastServerVal+bindTo), JsVar("s()."+bindTo))) // And remember what we sent so we can ignore it later
    partialUpdate(cmd)

    stateJson = mJs
    stateModel = m
  }

  private def fromClient(json:String) = {
//    implicit val formats = DefaultFormats
//    implicit val mf = manifest[String]
    import js.ToWithExtractMerged

    val jUpdate = JsonParser.parse(json) \\ "add"
    logger.debug("From Client: "+jUpdate)
    val updated = jUpdate.extractMerged(stateModel)
    logger.debug("From Client: "+updated)

    onClientUpdate(updated)
  }
}
