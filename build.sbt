
val copyJs = (resourceGenerators in Compile) += task {
  val jsSrcDir = sourceDirectory.value / "main" / "js"
  val jsDstDir = (resourceManaged in Compile).value
  val jsSrcs = IO.listFiles(jsSrcDir).filter(_.getName.endsWith(".js"))
  val srcRelNames = jsSrcs.map(_.getPath.substring(jsSrcDir.getPath.length+1))
  val withVer = srcRelNames.map(n => n.substring(0, n.length-3)+"-"+version.value+".js")
  val dstRelNames = withVer.map("toserve/net/liftmodules/ng/js/" + _)
  val jsDsts = dstRelNames.map(new File(jsDstDir, _))
  (jsSrcs zip jsDsts) map { case (src, dst) => IO.copyFile(src, dst) }
  jsDsts.toSeq
}

name := "ng"
organization := "net.liftmodules"
homepage := Some(url("https://github.com/joescii/lift-ng"))
version := "0.11.0"

val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")
val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")
liftVersion := (liftVersion ?? "3.2.0").value
liftEdition := liftVersion.value.substring(0,3)

name := name.value + "_" + liftEdition.value

// Necessary beginning with sbt 0.13, otherwise Lift editions get messed up.
// E.g. "2.5" gets converted to "2-5"
moduleName := name.value
crossScalaVersions := Seq("2.12.4", "2.11.12")
scalaVersion := crossScalaVersions.value.head

resolvers += "CB Central Mirror" at "http://repo.cloudbees.com/content/groups/public"
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
libraryDependencies := {
  // Ideally, keep this in sync with https://github.com/lift/framework/blob/master/project/Dependencies.scala#L32
  Seq(
    "net.liftweb"   %% "lift-webkit"  % liftVersion.value % "provided",
    "com.joescii"   %  "j2js-i18n"    % "0.1.1"           % "compile",
    "org.scalatest" %% "scalatest"    % "3.0.4"           % "test"
  )
}

scalacOptions :=
  "-deprecation" :: "-unchecked" :: "-feature" :: "-language:postfixOps" :: "-language:implicitConversions" :: Nil

excludeFilter in unmanagedSources := {
  HiddenFileFilter || 
//    (if(scalaVersion.value.startsWith("2.9")) "*2.10*" else "*2.9*") ||
    (if(liftEdition.value.startsWith("3")) "*2.x*" else "*3.x*")
}

buildInfoSettings
sourceGenerators in Compile += buildInfo
buildInfoKeys := Seq[BuildInfoKey](version, liftVersion, liftEdition)
buildInfoPackage := "net.liftmodules.ng"

publishTo := { version.value.endsWith("SNAPSHOT") match {
    case true  => Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    case false => Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  }
}
credentials += Credentials( file("sonatype.credentials") )
credentials += Credentials( file("/private/liftmodules/sonatype.credentials") )
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }

pomExtra := (
         <scm>
            <url>git@github.com:joescii/lift-ng.git</url>
            <connection>scm:git:git@github.com:joescii/lift-ng.git</connection>
         </scm>
         <developers>
            <developer>
              <id>htmldoug</id>
              <name>Doug Roper</name>
              <url>https://github.com/htmldoug</url>
            </developer>
            <developer>
              <id>joescii</id>
              <name>Joe Barnes</name>
              <url>https://github.com/joescii</url>
            </developer>
         </developers>
 )

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

Seq(com.untyped.sbtjs.Plugin.jsSettings : _*)
(sourceDirectory in (Compile, JsKeys.js)) := (sourceDirectory in Compile).value / "js"
(resourceManaged in (Compile, JsKeys.js)) := (resourceManaged in Compile).value / "toserve" / "net" / "liftmodules" / "ng" / "js"
(JsKeys.filenameSuffix in Compile) :=  "-" + version.value + ".min"
(resourceGenerators in Compile) += (JsKeys.js in Compile)
copyJs

// Jasmine stuff
Seq(jasmineSettings : _*)
appJsDir += sourceDirectory.value / "main" / "js"
appJsLibDir += sourceDirectory.value / "test" / "js" / "3rdlib"
jasmineTestDir += sourceDirectory.value / "test" / "js"
jasmineConfFile += sourceDirectory.value / "test" / "js" / "3rdlib" / "test.dependencies.js"
jasmineRequireJsFile += sourceDirectory.value / "test" / "js" / "3rdlib" / "require" / "require-2.0.6.js"
jasmineRequireConfFile += sourceDirectory.value / "test" / "js" / "3rdlib" / "require.conf.js"
