package net.liftmodules.ng

import js.JsonDeltaFuncs._
import net.liftweb._
import net.liftweb.json.{DefaultFormats, Formats, JsonParser, parse}
import json.Serialization._
import json.JsonAST._
import common._
import http.SHtml._
import http.js._
import http.S
import JE._
import net.liftweb.http.js.JsCmds._
import scala.xml.NodeSeq
import net.liftweb.http.js.JE.JsEq
import net.liftweb.http.js.JE.JsNotEq
import net.liftweb.json.JsonAST.JString
import net.liftweb.http.js.JsCmds.JsCrVar
import net.liftweb.common.Full
import net.liftweb.http.js.JE.JsVar
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmds.SetExp
import net.liftweb.json.JsonAST.JInt

/**
 * Simple binding actor for creating a binding actor in one line
 *
 * @param bindTo The client `\$scope` element to bind to
 * @param initialValue Initial value on session initialization
 * @param onClientUpdate Callback to execute on each update from the client
 * @param clientSendDelay Milliseconds for the client to delay sending updates, allowing them to batch into one request. Defaults to 1 second (1000)
 * @tparam M The type of the model to be used in this actor
 */
abstract class SimpleNgModelBinder[M <: Any : Manifest] (
  val bindTo:String,
  val initialValue:M,
  override val onClientUpdate:M=>M = { m:M => m },
  override val clientSendDelay:Int = 1000,
  override val formats:Formats = DefaultFormats
) extends NgModelBinder[M]{
  direction:BindDirection =>
}

private [ng] sealed trait BindingBase {
  /** The client `\$scope` element to bind to */
  def bindTo: String

  def formats:Formats

  /** The server-side state of the binder */
  private [ng] var stateJson: JValue
  /** Builds the $JsCmd to mutate the client state on server update */
  private [ng] def buildMutator(id:String, newState:JValue):JsCmd =
    JsCrVar("m", newState) &  // Name the state 'm'
    Call("e('"+id+"').injector().get('plumbing').inject", JsVar("m")) & // Inject promises
    SetExp(JsVar("s('"+id+"')." + bindTo), JsVar("m")) // Update the scope
  /** Builds the client-side update variable to send to the server on client-side update */
  private [ng] def buildClientUpdateVar:JsCmd = JsCrVar("u", Call("JSON.stringify", JsVar("n")))
  /** Converts the $JValue sent from the client to the server into the respective $NgModel */
  private [ng] def jValueToState[M <: Any](update:JValue, current:M)(implicit mf:Manifest[M]):M = {
    update.extract(formats, mf)
  }

  private [ng] def retainState:Boolean = false
}

/** Base trait for the two binding direction mixins */
sealed trait BindDirection {
  private [ng] def toClient = false
  private [ng] def toServer = false
}
/** Mix with your NgModelBinder to bind state from the server to the client */
trait BindingToClient extends BindDirection {
  private [ng] override val toClient = true
}
/** Mix with your NgModelBinder to bind state from the client to the server */
trait BindingToServer extends BindDirection {
  private [ng] override val toServer = true
}

/** Base trait for the scope of binding, either request (i.e. per page load) or session.  Default is request */
sealed trait BindingScope extends BindingBase {
  private [ng] def sessionScope = false
}
/** Mix with your NgModelBinder to extend the scope of the binder to the entire user session */
trait SessionScope extends BindingScope {
  private [ng] override def sessionScope = true
  private [ng] override def retainState = true
}

/** Mix with your NgModelBinder to optimize the binding and attempt to send the least amount of data needed.  Regard this as incomplete and experimental. */
trait BindingOptimizations extends BindingBase {
  private [ng] override def buildMutator(id:String, newState: JValue) = {
    val diff = stateJson dfn newState
    diff(JsVar("s('"+id+"')." + bindTo)) // Send the diff
  }
  private [ng] override def buildClientUpdateVar:JsCmd = JsCrVar("u", Call("JSON.stringify", JsVar("{add:n}")))
  private [ng] override def jValueToState[M <: Any](update:JValue, current:M)(implicit mf:Manifest[M]):M = {
    import js.ToWithExtractMerged
    val added = Json.slash(update, "add")
    added.extractMerged(current)(mf, formats)
  }

  private [ng] override def retainState = true
}

/**
  * CometActor which implements binding to a model in the target \$scope.
  * While a trait would be preferable, we need the type constraint in order
  * for lift-json to deserialize messages from the client.
  * @tparam M The type of the model to be used in this actor
  */
abstract class NgModelBinder[M <: Any : Manifest] extends AngularActor with BindingBase with BindingScope {
  self:BindDirection  =>
  import Angular._

  private [ng] def buildMutator(newState:JValue):JsCmd = buildMutator(id, newState)
  private [ng] val jsScope = "s('"+id+"')"

  /** Initial value on session initialization */
  def initialValue: M

  /** Milliseconds for the client to delay sending updates, allowing them to batch into one request */
  def clientSendDelay: Int = 1000

  /** Callback to execute on each update from the client */
  def onClientUpdate: M => M = {m:M => m}

  // This must be lazy so that it is invoked only after name is set.
  private lazy val guts =
    if(toServer && toClient && sessionScope)
      if(name.isDefined) new TwoWaySessionNamed else new TwoWaySessionUnnamed
    else if(toServer && toClient && !sessionScope)
      new TwoWayRequestScoped
    else if(toClient)
      new ToClientGuts
    else
      new ToServerGuts

  override def fixedRender = nodesToRender ++ guts.render

  override def lowPriority = guts.receive

  /** Abstracting the guts of our actor. */
  private[ng] trait BindingGuts {
    def receive: PartialFunction[Any, Unit]

    def render: NodeSeq

  }

  /** Called after an update from Client.  Input is the client ID where the update originated from */
  private type AfterUpdateFn = Box[String] => Unit
  /** Called to send a JsCmd to the client */
  private type SendCmdFn = JsCmd => Unit
  /** Called to handle JSON from the client */
  private type JsonHandlerFn = String => Unit

  private class ToServerGuts extends BindingGuts {
    override def render = Script(buildCmd(root = false,
      renderCurrentState &
      renderThrottleCount &
      watch(timeThrottledCall(sendToServer(handleJson)))
      // TODO: Figure out how to ignore initial $watch
    ))

    private def handleJson:JsonHandlerFn = { json =>
      fromClient(json, Empty, afterUpdate)
    }

    override def receive = PartialFunction.empty
    private def afterUpdate:AfterUpdateFn = id => {}
  }

  private class ToClientGuts extends BindingGuts {
    override def render = Script(buildCmd(root = false, renderCurrentState))

    override def receive = receiveFromServer(sendToClient) orElse receiveToClient

    private def sendToClient:SendCmdFn = cmd => self ! ToClient(cmd)
  }

  private class TwoWayRequestScoped extends BindingGuts {
    override def render = Script(buildCmd(root = false,
      renderCurrentState &
      renderThrottleCount &
      SetExp(JsVar(jsScope+"." + lastServerVal + bindTo), JsVar(jsScope+"." + bindTo)) & // This prevents us from sending a server-sent value back to the server when doing 2-way binding
      watch(ifNotServerEcho(timeThrottledCall(sendToServer(handleJson))))
    ))

    private def handleJson:JsonHandlerFn = { json =>
      fromClient(json, Empty, afterUpdate)
    }

    private def afterUpdate:AfterUpdateFn = id => {}

    override def receive = receiveFromServer(sendToClient) orElse receiveToClient

    private def sendToClient:SendCmdFn = cmd => self ! ToClient(cmd)
  }

  /** Guts for the unnamed binding actor which exits per session and allows the models to be bound together */
  private class TwoWaySessionUnnamed extends BindingGuts {

    override def render = Script(buildCmd(root = false,
      renderCurrentState &
      renderThrottleCount &
      SetExp(JsVar(jsScope+"." + lastServerVal + bindTo), JsVar(jsScope+"." + bindTo)) // This prevents us from sending a server-sent value back to the server when doing 2-way binding
    ))

    override def receive = receiveFromServer(sendToClient(Empty)) orElse receiveFromClient(afterUpdate)

    private def sendToClient(exclude:Box[String]):SendCmdFn = { cmd =>
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

    private def afterUpdate(exclude:Box[String]): Unit = {
      val cmd = buildMutator(stateJson) &
        SetExp(JsVar(jsScope+"." + lastServerVal + bindTo), JsVar(jsScope+"." + bindTo)) // And remember what we sent so we can ignore it later

      sendToClient(exclude)(cmd)
    }
  }

  /** Guts for the named binding actor which exists per page and facilitates models to a given rendering of the page */
  private class TwoWaySessionNamed extends BindingGuts {
    override def render = Script(buildCmd(root = false, watch(ifNotServerEcho(timeThrottledCall(sendToServer(sendToSession))))))

    override def receive = receiveToClient

    private def sendToSession:JsonHandlerFn = json => for {
      session <- S.session
      cometType <- theType
      comet <- session.findComet(cometType, Empty)
      clientId <- name
    } { comet ! FromClient(json, Full(clientId)) }
  }

  private val lastServerVal = "net_liftmodules_ng_last_val_"
  private val queueCount = "net_liftmodules_ng_queue_count_"

  private var stateModel: M = initialValue
  private [ng] var stateJson = toJValue(stateModel)

  private def receiveFromClient(afterUpdate: AfterUpdateFn): PartialFunction[Any, Unit] = {
    case FromClient(json, id) => fromClient(json, id, afterUpdate)
  }

  private def receiveFromServer(sendFn: SendCmdFn): PartialFunction[Any, Unit] = {
    case m: M => fromServer(m, sendFn)
  }

  private def receiveToClient: PartialFunction[Any, Unit] = {
    case ToClient(cmd) => partialUpdate(buildCmd(root = false, cmd))
  }

  private def fromServer(m: M, sendFn: SendCmdFn) = {
    val mJs = toJValue(m)
    val cmd = buildMutator(mJs)
    sendFn(cmd)
    if(retainState) {
      stateJson = mJs
      stateModel = m
    }
  }

  private def fromClient(json: String, clientId:Box[String], afterUpdate: AfterUpdateFn) = {
    val parsed = JsonParser.parse(json)
    val updated = jValueToState(parsed, stateModel)
    logger.debug("From Client: " + updated)

    // TODO: Do something with the return value, or change it to return unit?
    onClientUpdate(updated)

    if(retainState) {
      // TODO: When jUpdate becomes a diff, make sure we have the whole thing
      stateModel = updated
      stateJson = toJValue(updated)
    }

    afterUpdate(clientId)
  }

  private def toJValue(m: M): JValue = {
    m match {
      case m if m != null => parse(stringify(m)(formats))
      case e => JNull
    }
  }

  private def renderCurrentState = SetExp(JsVar(jsScope+"." + bindTo), stateJson) & // Send the current state with the page
    Call("e('"+id+"').injector().get('plumbing').inject", JsVar(jsScope+"." + bindTo)) // Inject any promises we're sending
  private val renderThrottleCount = SetExp(JsVar(jsScope+"." + queueCount + bindTo), JInt(0)) // Set the last server val to avoid echoing it back

  private def watch(f:JsCmd):JsCmd = Call(jsScope+".$watch", JString(bindTo), AnonFunc("n,o", f), JsTrue) // True => Deep comparison

  private def ifNotServerEcho(f:JsCmd):JsCmd =
  // If the new value, n, is not equal to the last server val, send it.
    JsIf(JsNotEq(JsVar("n"), JsRaw(jsScope+"." + lastServerVal + bindTo)),
      f,
      // else remove our last saved value so we can forget about it
      SetExp(JsVar(jsScope+"." + lastServerVal + bindTo), JsNull)
    )

  private def timeThrottledCall(f:JsCmd):JsCmd =
    JsCrVar("c", JsVar(jsScope + "." + queueCount + bindTo + "++")) &
      Call("setTimeout", AnonFunc(
        JsIf(JsEq(JsVar("c+1"), JsVar(jsScope + "." + queueCount + bindTo)), f)
      ), JInt(clientSendDelay))

  private def sendToServer(handler: JsonHandlerFn):JsCmd = buildClientUpdateVar &
    ajaxCall(JsVar("u"), jsonStr => {
      logger.debug("Received string: "+jsonStr)
      handler(jsonStr)
      Noop
    })
}

private[ng] case class FromClient(json: String, clientId: Box[String])
private[ng] case class ToClient(cmd: JsCmd)

