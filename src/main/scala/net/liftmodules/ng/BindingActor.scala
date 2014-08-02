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
  /** Initial value on session initialization */
  def initialValue:M
  /** Milliseconds for the client to delay sending updates, allowing them to batch into one request */
  def clientSendDelay:Int = 1000

  private val lastServerVal = "net_liftmodules_ng_last_val_"
  private val clientId = "net_liftmodules_ng_client_id_"
  private val queueCount = "net_liftmodules_ng_queue_count_"

  def onClientUpdate(m:M):M = m

  case class ClientJson(json:String)

  var stateModel:M = initialValue
  var stateJson:JValue = JNull

  override def fixedRender = nodesToRender ++ Script(buildCmd(root = false,
    SetExp(JsVar("s()."+bindTo), stateJson) & // Send the current state with the page
    SetExp(JsVar("s()."+lastServerVal+bindTo), JsVar("s()."+bindTo)) & // Set the last server val to avoid echoing it back
    SetExp(JsVar("s()."+clientId+bindTo), JString(rand)) &
    SetExp(JsVar("s()."+queueCount+bindTo), JInt(0)) &
    Call("s().$watchCollection", JString(bindTo), AnonFunc("n,o",
      // If the new value, n, is not equal to the last server val, send it.
      JsIf(JsNotEq(JsVar("n"), JsRaw("s()."+lastServerVal+bindTo)),
        JsCrVar("c", JsVar("s()."+queueCount+bindTo+"++")) &
        Call("setTimeout", AnonFunc(
          Call("console.log", JsVar("c")) &
          JsIf(JsEq(JsVar("c+1"), JsVar("s()."+queueCount+bindTo)),
            JsCrVar("u", Call("JSON.stringify", JsRaw("{add:n,id:s()."+clientId+bindTo+"}"))) &
            ajaxCall(JsVar("u"), s => {
              this ! ClientJson(s)
              Noop
            })
          )
        ), JInt(clientSendDelay)),
        // else remove our last saved value so we can forget about it
      SetExp(JsVar("s()."+lastServerVal+bindTo), JsNull)
    )))
  ))

  def toJValue(m:Any):JValue = m match {
    case m:NgModel  => parse(write(m))
    case e => JNull
  }

  private implicit val formats = DefaultFormats
  override def lowPriority = {
    case ClientJson(json) => fromClient(json)
    case m:M => fromServer(m)
    case e => logger.warn("Received un-handled model '"+e+"' of type '"+e.getClass.getName+"'.")
  }

  private def fromServer(m:M) = {
    val mJs = toJValue(m)
    sendDiff("server", mJs)
    stateJson = mJs
    stateModel = m
  }

  private def sendDiff(id:String, mJs:JValue) = {
    val diff = stateJson dfn mJs
    val cmd =
      JsIf(JsNotEq(JString(id), JsVar("s()."+clientId+bindTo)),
        buildCmd(root = false, diff(JsVar("s()."+bindTo)) & // Send the diff
        SetExp(JsVar("s()."+lastServerVal+bindTo), JsVar("s()."+bindTo))) // And remember what we sent so we can ignore it later
      )
    partialUpdate(cmd)
  }

  private def fromClient(json:String) = {
//    implicit val formats = DefaultFormats
//    implicit val mf = manifest[String]
    import js.ToWithExtractMerged

    val parsed = JsonParser.parse(json)
    val jUpdate = parsed \\ "add"
    val id = (parsed \\ "id").extract[String]
    logger.debug("From Client ("+id+"): "+jUpdate)
    val updated = jUpdate.extractMerged(stateModel)
    logger.debug("From Client ("+id+"): "+updated)

    val mJs = jUpdate

    sendDiff(id, mJs)

    // TODO: Do something with the return value, or change it to return unit?
    onClientUpdate(updated)

    // TODO: When jUpdate becomes a diff, make sure we have the whole thing
    stateJson = jUpdate
    stateModel = updated
  }
}
