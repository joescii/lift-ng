package net.liftmodules.ng
package comet

import Angular.{FutureId, ReturnData}
import net.liftweb._
import http._
import common._
import js._
import JE._
import JsCmds._
import net.liftweb.json.{Formats, JsonAST}

import scala.xml.NodeSeq

class LiftNgFutureActor extends CometActor {
  def render = NodeSeq.Empty

  def callback[T <: Any](id: FutureId, box: => Box[T], formats: Formats) = partialUpdate {
    val promise = Angular.DefaultApiSuccessMapper.boxToPromise(box)(formats)
    val response = JsonAST.compactRender(promiseToJson(promise))
    val js = s"""net_liftmodules_ng.processComet("${Angular.appSelectorDefault}",$response,"$id");"""
    JsRaw(js)
  }

  override def lowPriority = {
    case ReturnData(id, box, formats) => callback(id, box, formats)
  }
}
