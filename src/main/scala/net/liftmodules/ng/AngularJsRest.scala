package net.liftmodules.ng

import net.liftweb.common.{Loggable, Full, Box}
import net.liftweb.http.LiftRules.SplitSuffixPF
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper

object AngularJsRest extends RestHelper with Loggable {
  def init() {
    LiftRules.suffixSplitters.prepend(splitter)
    LiftRules.statelessDispatch.append(this)

    val version = AngularJsRest.webjar.openOrThrowException(
      "lift-ng has been initialized with includeAngularJs==true but it appears you do not have the angularjs webjar configured in your classpath"
    ).version
    logger.info("Serving angular version "+version)
  }

  case class WebJarInfo(version:String, angularDir:String, modulesAreInSeparateJars:Boolean)
  lazy val version = webjar.openOrThrowException(
    "lift-ng has been initialized with includeAngularJs==true but it appears you do not have the angularjs webjar configured in your classpath"
  ).version
  private [ng] lazy val webjar:Box[WebJarInfo] = {
    val VersionRegex = """^\Qversion=\E(.*)$""".r

    def webJarInfo(pkg:String, angular:String, modulesAreInSeparateJars:Boolean):Box[WebJarInfo] = for {
      props <- LiftRules.loadResourceAsString("/META-INF/maven/"+pkg+"/"+angular+"/pom.properties")
      version <- props.lines.collectFirst { case VersionRegex(v) => v }
      _ <- LiftRules.loadResourceAsString("/META-INF/resources/webjars/"+angular+"/"+version+"/angular.js")
    } yield { WebJarInfo(version, angular, modulesAreInSeparateJars) }

    lazy val classicWebJar:Box[WebJarInfo] = webJarInfo("org.webjars",       "angularjs", false)
    lazy val bowerWebJar1:Box[WebJarInfo]  = webJarInfo("org.webjars.bower", "angularjs", true)
    lazy val bowerWebJar2:Box[WebJarInfo]  = webJarInfo("org.webjars.bower", "angular", true)
    lazy val npmWebJar:Box[WebJarInfo]     = webJarInfo("org.webjars.npm",   "angular", true)

    Stream(classicWebJar, bowerWebJar1, bowerWebJar2, npmWebJar).collectFirst { case Full(info) => info }
  }

  private def pathFor(assetName:String):Box[String] = for {
    info <- webjar
  } yield {
    import info._

    val beforeExtension = assetName.split('.').head

    // Either use angular/angularjs in the path if...
    val useAngularInPath = !modulesAreInSeparateJars ||
      beforeExtension == "angular" ||
      beforeExtension == "angular-csp"

    val pkg = if(useAngularInPath) angularDir else beforeExtension
    val path = "/META-INF/resources/webjars/"+pkg+"/"+info.version+"/"+assetName

    path
  }

  private def response(assetName:String):LiftResponse = (for {
    path <- pathFor(assetName)
    asset <- LiftRules.loadResourceAsString(path)
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
    // The reason for having version is the path is to ensure proper cache behavior
    case "net" :: "liftmodules" :: "ng" :: "angular-js" :: version :: name :: Nil Get _ =>
      if(webjar.map(_.version) == Full(version)) response(name)
      else NotFoundResponse()
  }

}
