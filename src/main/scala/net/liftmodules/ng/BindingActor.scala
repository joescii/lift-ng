package net.liftmodules.ng

import Angular.NgModel
import js.JsonDeltaFuncs._
import net.liftweb._
import json.{JsonParser, DefaultFormats, parse}
import json.Serialization._
import json.JsonAST._
import common._
import http.SHtml._
import http.js._
import http.S
import JE._
import JsCmds._
import scala.xml.NodeSeq

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

private[ng] case class FromClient(json: String, clientId: String)
private[ng] case class ToClient(cmd: JsCmd)

/** CometActor which implements binding to a model in the target $scope */
abstract class BindingActor[M <: NgModel : Manifest] extends AngularActor {
  import Angular._

  /** The client `\$scope` element to bind to */
  def bindTo: String

  /** Initial value on session initialization */
  def initialValue: M

  /** Milliseconds for the client to delay sending updates, allowing them to batch into one request */
  def clientSendDelay: Int = 1000

  /** Callback to execute on each update from the client */
  def onClientUpdate: M => M = {m:M => m}

  // This must be lazy so that it is invoked only after name is set.
  lazy val guts = if(name.isDefined) new PageBinder else new SessionBinder

  override def fixedRender = nodesToRender ++ guts.render

  override def lowPriority = guts.receive

  /** Abstracting the guts of our actor. */
  private[ng] trait BindingGuts {
    def receive: PartialFunction[Any, Unit]

    def render: NodeSeq

    val lastServerVal = "net_liftmodules_ng_last_val_"
    val queueCount = "net_liftmodules_ng_queue_count_"

  }



  /** Guts for the unnamed binding actor which exits per session and allows the models to be bound together */
  private[ng] class SessionBinder extends BindingGuts {
    var stateModel: M = initialValue
    var stateJson: JValue = JNull

    override def render = Script(buildCmd(root = false,
      SetExp(JsVar("s()." + bindTo), stateJson) & // Send the current state with the page
      SetExp(JsVar("s()." + lastServerVal + bindTo), JsVar("s()." + bindTo)) & // Set the last server val to avoid echoing it back
      SetExp(JsVar("s()." + queueCount + bindTo), JInt(0)) // This prevents us from sending a server-sent value back to the server
    ))

    override def receive: PartialFunction[Any, Unit] = {
      case FromClient(json, id) => fromClient(json, id)
      case m: M => fromServer(m)
      case e => logger.warn("Received un-handled model '" + e + "' of type '" + e.getClass.getName + "'.")
    }

    private implicit val formats = DefaultFormats

    private def toJValue(m: Any): JValue = m match {
      case m: NgModel => parse(write(m))
      case e => JNull
    }

    private def fromServer(m: M) = {
      val mJs = toJValue(m)
      sendDiff(mJs, Empty)
      stateJson = mJs
      stateModel = m
    }

    private def sendDiff(mJs: JValue, exclude:Box[String]) = {
      val diff = stateJson dfn mJs
      val cmd =
        diff(JsVar("s()." + bindTo)) & // Send the diff
        SetExp(JsVar("s()." + lastServerVal + bindTo), JsVar("s()." + bindTo)) // And remember what we sent so we can ignore it later

      for {
        t <- theType
        session <- S.session
        comet <- session.findComet(t)
        if comet.name.isDefined  // Never send to unnamed comet. It doesn't handle these messages.
        if comet.name != exclude // Send to all clients but the originating client (if not Empty)
      } { comet ! ToClient(cmd) }

      // If we don't poke, then next time we are rendered, it won't contain the latest state
      poke()
    }

    private def fromClient(json: String, clientId:String) = {
      //    implicit val formats = DefaultFormats
      //    implicit val mf = manifest[String]
      import js.ToWithExtractMerged

      val parsed = JsonParser.parse(json)
      val jUpdate = parsed \\ "add"
      logger.debug("From Client: " + jUpdate)
      val updated = jUpdate.extractMerged(stateModel)
      logger.debug("From Client: " + updated)

      // TODO: Do something with the return value, or change it to return unit?
      onClientUpdate(updated)

      val mJs = toJValue(updated)
      sendDiff(mJs, Full(clientId))

      // TODO: When jUpdate becomes a diff, make sure we have the whole thing
      stateJson = mJs
      stateModel = updated
    }
  }



  /** Guts for the named binding actor which exists per page and facilitates models to a given rendering of the page */
  private[ng] class PageBinder extends BindingGuts {
    // TODO: Move all this crap to our js file
    override def render = Script(buildCmd(root = false,
      Call("s().$watch", JString(bindTo), AnonFunc("n,o",
        // If the new value, n, is not equal to the last server val, send it.
        JsIf(JsNotEq(JsVar("n"), JsRaw("s()." + lastServerVal + bindTo)),
          JsCrVar("c", JsVar("s()." + queueCount + bindTo + "++")) &
            Call("setTimeout", AnonFunc(
              JsIf(JsEq(JsVar("c+1"), JsVar("s()." + queueCount + bindTo)),
                JsCrVar("u", Call("JSON.stringify", JsVar("{add:n}"))) &
                  ajaxCall(JsVar("u"), jsonStr => {
                    logger.debug("Received string: "+jsonStr)
                    sendToSession(jsonStr)
                    Noop
                  })
              )
            ), JInt(clientSendDelay)),
          // else remove our last saved value so we can forget about it
          SetExp(JsVar("s()." + lastServerVal + bindTo), JsNull)
        )), JsTrue) // True => Deep comparison
    ))

    override def receive: PartialFunction[Any, Unit] = {
      case ToClient(cmd) => partialUpdate(buildCmd(root = false, cmd))
    }

    private def sendToSession(json:String) = for {
      session <- S.session
      cometType <- theType
      comet <- session.findComet(cometType, Empty)
      clientId <- name
    } { comet ! FromClient(json, clientId) }

  }
}