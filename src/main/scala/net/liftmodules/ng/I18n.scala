package net.liftmodules.ng

import java.net.URLEncoder

import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import com.joescii.j2jsi18n.JsResourceBundle

import scala.xml.NodeSeq
import net.liftweb.http.rest.RestHelper
import java.security.MessageDigest
import java.math.BigInteger

import net.liftweb.http.js.JE
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JE.JsVar
import net.liftweb.http.js.JE.JsRaw

object AngularI18n extends DispatchSnippet {
  /** Implementation of dispatch to allow us to add ourselves as a snippet */
  override def dispatch = {
    case _ => { _ => render }
  }

  def render:NodeSeq = {
    val fromName  = S.attr("name").map(_.toString).toList
    val fromNames = S.attr("names").map(_.toString.split(',')).toList.flatten
    val names = fromName ++ fromNames
    val src =
      "/net/liftmodules/ng/i18n?"+
        (names.map(n => "name="+URLEncoder.encode(n, "UTF-8")).mkString("&"))+
        "&loc="+S.locale.toString+
        "&sum="+(module(names, S.locale.toString).digest)
    <script src={src}></script>
  }

  case class Module(js:JE.Call, digest:String)

  private [this] val moduleCache: ConcurrentHashMap[(List[String], String), Module] = new ConcurrentHashMap()

  def module(names: List[String], loc: String) =
    // This technically has a race condition, but the worst thing that will happen is the same Module
    // gets built twice.
    if(moduleCache.containsKey((names, loc))) moduleCache.get((names, loc))
    else {
      val moduleJs = toModule(names)
      val sum = digest(moduleJs.toString)
      val module = Module(moduleJs, sum)

      moduleCache.put((names, loc), module)
      module
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
}

object AngularI18nRest extends RestHelper {
  def init() {
    LiftRules.statelessDispatch.append(this)
  }

  serve {
    case "net" :: "liftmodules" :: "ng" :: "i18n" :: Nil Get _ => {
      val names = S.params("name")
      val res = S.param("loc").map { loc =>
        new JavaScriptResponse(AngularI18n.module(names, loc).js, Nil, Nil, 200)
      }.openOr(NotFoundResponse())
      res
    }
  }
}