package net.liftmodules.ng

import Angular.NgModel
import net.liftweb.json.{ DefaultFormats, parse }
import net.liftweb.json.Serialization._
import net.liftweb.json.JsonAST._
import net.liftweb.http.js.JE._
import js.JsonDeltaFuncs._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.SHtml._

trait BindingActor extends AngularActor {
  /** The client `\$scope` element to bind to */
  def bindTo:String

  var stateModel:Any = null
  var stateJson:JValue = JNull

  override def render = nodesToRender ++ Script(buildCmd(root = false,
    SetExp(JsVar("s."+bindTo), stateJson) &
    Call("s.$watch", JString(bindTo), AnonFunc("updated",
      Call("console.log", JsVar("updated")) &
      ajaxCall(JsVar("updated"), s => {
        logger.debug(s)
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
    case m:NgModel  => doEverything(m)
    case s:String   => doEverything(s)
    case i:Int      => doEverything(i)
    case e => logger.warn("Received un-handled model '"+e+"' of type '"+e.getClass.getName+"'.")
  }

  private def doEverything(m:Any) = {
    val mJs = toJValue(m)
    val diff = stateJson dfn mJs
    val cmd = buildCmd(root = false, diff(JsVar("s."+bindTo)))
    partialUpdate(cmd)

    stateJson = mJs
    stateModel = m
  }
}
