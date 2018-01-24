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
    val js =
      "var es=document.querySelectorAll('"+Angular.appSelectorDefault+"');"+
      "var e,i,l;" +
      "i=0;" +
      "for(l=es.length;i<l;i++){" +
        "e=angular.element(es[i]);" +
        "e.scope().$apply(function(){e.injector().get('plumbing').fulfill("+response+",'"+id+"')});" +
      "}"
    JsRaw(js)
  }

  override def lowPriority = {
    case ReturnData(id, box, formats) => callback(id, box, formats)
  }
}
