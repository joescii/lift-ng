package net.liftmodules.ng

import net.liftweb.common.Box
import net.liftweb.http.LiftRules.SplitSuffixPF
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper

object AngularJsRest extends RestHelper {
  def init() {
    LiftRules.suffixSplitters.prepend(splitter)
    LiftRules.statelessDispatch.append(this)
  }

  private val VersionRegex = """^\Qversion=\E(.*)$""".r

  private [ng] lazy val angularWebjarVersion:Box[String] = for {
    props <- LiftRules.loadResourceAsString("/META-INF/maven/org.webjars/angularjs/pom.properties")
    version <- props.lines.collectFirst { case VersionRegex(v) => v }
  } yield { version }

  private [ng] lazy val angularWebjarPath:Box[String] = for {
    version <- angularWebjarVersion
  } yield { "/META-INF/resources/webjars/angularjs/"+version }

  private def response(assetName:String):LiftResponse = (for {
    path <- angularWebjarPath
    asset <- LiftRules.loadResourceAsString(path + "/" + assetName)
  } yield {
    val cType = assetName.split("\\.").last match {
      case "js"           => "application/javascript"
      case "map" | "json" => "application/json"
      case "css"          => "text/css"
      case _              => "text/plain"
    }

    InMemoryResponse(
      data = asset.getBytes("utf-8"),
      headers = List(("Content-type", cType)),
      cookies = List(),
      code = 200
    )
  }).openOr(NotFoundResponse())

  val path = "/net/liftmodules/ng/angular-js/"

  // Don't split the file suffix out for our assets
  private val splitter:SplitSuffixPF = {
    case parts @ ("net" :: "liftmodules" :: "ng" :: "angular-js" :: _) => (parts, "")
  }

  serve {
    case "net" :: "liftmodules" :: "ng" :: "angular-js" :: name :: Nil Get _ =>
      response(name)
  }

}
