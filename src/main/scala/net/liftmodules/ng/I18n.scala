package net.liftmodules.ng

import net.liftweb.http.{JavaScriptResponse, LiftRules, DispatchSnippet, S}
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import com.joescii.j2jsi18n.JsResourceBundle
import scala.xml.NodeSeq
import net.liftweb.http.rest.RestHelper
import java.security.MessageDigest
import java.math.BigInteger

object AngularI18n extends DispatchSnippet {
  /** Implementation of dispatch to allow us to add ourselves as a snippet */
  override def dispatch = {
    case _ => { _ => render }
  }

  def render:NodeSeq = {
    val fromName  = S.attr("name").map(_.toString).toList
    val fromNames = S.attr("names").map(_.toString.split(',')).toList.flatten
    val names = fromName ++ fromNames
    val src = "lift-ng/i18n?"+(names.map(urlParam).mkString("&"))
    <script src={src}></script>
  }

  def toModule(names:List[String]) = {
    val rsrcs = LiftRules.resourceNames.zip(S.resourceBundles).filter{ case (name, b) => names.contains(name) }.toMap
    val moduleDeclaration = Call("angular.module", "i18n", JsArray())
    rsrcs.foldLeft(moduleDeclaration){ case (module, (name, bundle)) =>
      val jsb = new JsResourceBundle(bundle)
      Call(JsVar(module.toJsCmd, "factory").toJsCmd, name, AnonFunc(JsReturn(JsRaw(jsb.toJs))))
    }
  }

  def digest(str:String):String = {
    val md5 = MessageDigest.getInstance("MD5")
    val digest = md5.digest(str.getBytes("UTF-8"))
    new BigInteger(1, digest).toString(16)
  }

  def urlParam(name:String):String = {
    val module = toModule(List(name)).toString()
    println(module)
    val sum = digest(module)
    "name="+name+"&sum="+sum
  }
}

object AngularI18nRest extends RestHelper {
  def init() {
    LiftRules.statelessDispatch.append(AngularI18nRest)
  }

  serve {
    case "lift-ng" :: "i18n" :: Nil Get _ => {
      val names = S.params("name")
      new JavaScriptResponse(AngularI18n.toModule(names), Nil, Nil, 200)
    }
  }
}