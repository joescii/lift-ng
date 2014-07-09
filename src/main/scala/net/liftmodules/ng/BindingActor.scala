package net.liftmodules.ng

import Angular.NgModel
import net.liftweb.json.{ DefaultFormats, parse }
import net.liftweb.json.Serialization._
import net.liftweb.json.JsonAST._
import net.liftweb.http.js.JE.JsVar
import js.JsonDeltaFuncs._

trait BindingActor extends AngularActor {
  /** The client `\$scope` element to bind to */
  def bindTo:String

  var stateModel:Any = null
  var stateJson:JValue = JNull

  private implicit val formats = DefaultFormats
  override def lowPriority = {
    case m:NgModel => doEverything(m, parse(write(m)))
    case s:String  => doEverything(s, JString(s))
    case i:Int     => doEverything(i, JInt(i))
    case e => logger.warn("Received un-handled model '"+e+"' of type '"+e.getClass.getName+"'.")
  }

  private def doEverything(m:Any, mJs:JValue) = {
    val diff = stateJson dfn mJs
    val cmd = buildCmd(false, diff(JsVar("s."+bindTo)))
    partialUpdate(cmd)

    stateJson = mJs
    stateModel = m
  }
}
