package net.liftmodules.ng

import Angular.NgModel
import net.liftweb.json.{JsonParser, DefaultFormats, parse}
import net.liftweb.json.Serialization._
import net.liftweb.json.JsonAST._
import net.liftweb.http.js.JE._
import js.JsonDeltaFuncs._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.SHtml._

/**
 * Simple binding actor for creating a binding actor in one line
 *
 * @param bindTo The client `\$scope` element to bind to
 * @param initialValue Initial value on session initialization
 * @param onClientUpdate Callback to execute on each update from the client
 * @param clientSendDelay Milliseconds for the client to delay sending updates, allowing them to batch into one request. Defaults to 1 second (1000)
 * @tparam M The type of the model to be used in this actor
 */
abstract class SimpleBindingActor[M <: NgModel : Manifest]
  (val bindTo:String, val initialValue:M, override val onClientUpdate:M=>M = { m:M => m }, override val clientSendDelay:Int = 1000)
  extends BindingActor[M]{}

/** CometActor which implements binding to a model in the target $scope */
abstract class BindingActor[M <: NgModel : Manifest] extends AngularActor {
  /** The client `\$scope` element to bind to */
  def bindTo:String
  /** Initial value on session initialization */
  def initialValue:M
  /** Milliseconds for the client to delay sending updates, allowing them to batch into one request */
  def clientSendDelay:Int = 1000

  private val lastServerVal = "net_liftmodules_ng_last_val_"
  private val queueCount = "net_liftmodules_ng_queue_count_"

  /** Callback to execute on each update from the client */
  def onClientUpdate:M=>M = { m:M => m }

  case class ClientJson(json:String)

  var stateModel:M = initialValue
  var stateJson:JValue = JNull

  override def fixedRender = nodesToRender ++ (if(name.isDefined) namedRender else unnamedRender)

  // TODO: Move all this crap to our js file
  private def namedRender = Script(buildCmd(root = false,
    Call("s().$watchCollection", JString(bindTo), AnonFunc("n,o",
      // If the new value, n, is not equal to the last server val, send it.
      JsIf(JsNotEq(JsVar("n"), JsRaw("s()."+lastServerVal+bindTo)),
        JsCrVar("c", JsVar("s()."+queueCount+bindTo+"++")) &
        Call("setTimeout", AnonFunc(
          JsIf(JsEq(JsVar("c+1"), JsVar("s()."+queueCount+bindTo)),
            JsCrVar("u", Call("JSON.stringify", JsRaw("{add:n}"))) &
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

  private def unnamedRender = Script(buildCmd(root = false,
    SetExp(JsVar("s()."+bindTo), stateJson) & // Send the current state with the page
    SetExp(JsVar("s()."+lastServerVal+bindTo), JsVar("s()."+bindTo)) & // Set the last server val to avoid echoing it back
    SetExp(JsVar("s()."+queueCount+bindTo), JInt(0)) // This prevents us from sending a server-sent value back to the server
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
    sendDiff(mJs)
    stateJson = mJs
    stateModel = m
  }

  private def sendDiff(mJs:JValue) = {
    val diff = stateJson dfn mJs
    val cmd = buildCmd(root = false, diff(
      JsVar("s()."+bindTo)) & // Send the diff
      SetExp(JsVar("s()."+lastServerVal+bindTo), JsVar("s()."+bindTo)) // And remember what we sent so we can ignore it later
    )
    partialUpdate(cmd)
  }

  private def fromClient(json:String) = {
//    implicit val formats = DefaultFormats
//    implicit val mf = manifest[String]
    import js.ToWithExtractMerged

    val parsed = JsonParser.parse(json)
    val jUpdate = parsed \\ "add"
    logger.debug("From Client: "+jUpdate)
    val updated = jUpdate.extractMerged(stateModel)
    logger.debug("From Client: "+updated)

    // TODO: If we have some kind of session sync mode, then send it to other comets

    // TODO: Do something with the return value, or change it to return unit?
    onClientUpdate(updated)

    // TODO: When jUpdate becomes a diff, make sure we have the whole thing
    stateJson = jUpdate
    stateModel = updated
  }
}
