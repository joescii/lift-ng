package net.liftmodules.ng

import net.liftweb.http._
import net.liftweb.http.S._
import net.liftweb.http.js.{JsExp}
import net.liftweb.http.js.JE.JsRaw
import AFuncHolder._
import net.liftweb.json.JsonAST.JObject

/**
 * CareLiftUtil functions that augment the Lift SHtml object
 */
object SHtmlExtensions extends SHtml {

  /**
   * Registers a server-side function that takes a single string parameter and returns a json object string. The
   * function may be invoked POST'ing the result of the returned JsExp as form data to
   * ('/ajax_request/' + lift_page + '/').
   */
  def ajaxJsonPost(
    jsCalcValue: JsExp,
    jsonFunc: String => JObject
    ): JsExp = {
    val jsonResponseFunc: (String) => LiftResponse = jsonFunc.andThen(toJsonResponse)
    fmapFunc(jsonResponseFunc)(name => JsRaw("{name:'" + name + "',data:" + jsCalcValue.toJsCmd + "}"))
  }

  /**
   * Registers a server-side function that takes no arguments and returns a json object string. The function may be
   * invoked POST'ing the returned String as form data to ('/ajax_request/' + lift_page + '/').
   */
  def ajaxJsonPost(jsonFunc: String => JObject): JsExp = {
    val jsonResponseFunc: (String) => LiftResponse = jsonFunc.andThen(toJsonResponse)
    fmapFunc(jsonResponseFunc)(name => JsRaw("{name:'"+name+"'}"))
  }

  private val toJsonResponse = (jsonObjFunc: JObject) => JsonResponse.apply(jsonObjFunc)

}
