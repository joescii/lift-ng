name := "ng-test"

organization := "net.liftmodules"

version := "0.6.2"

liftVersion := System.getProperty("lift.version", "2.5.1")

liftEdition <<= liftVersion { _.substring(0,3) }

scalaVersion := System.getProperty("scala.version", "2.10.4")

scalacOptions ++= Seq("-deprecation", "-unchecked")

resolvers ++= Seq(
  "staging"   at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "https://oss.sonatype.org/content/repositories/releases"
)

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

libraryDependencies <++= (liftVersion, version) { (lift, liftng) =>
  val liftEdition = lift.substring(0,3)
  Seq(
    "net.liftweb"             %%  "lift-webkit"                       % lift                  % "compile",
    "net.liftmodules"         %%  ("lift-jquery-module_"+liftEdition) % "2.9-SNAPSHOT"                 % "compile",
    "net.liftmodules"         %%  ("ng_"+liftEdition)                 % liftng                % "compile", // https://github.com/joescii/lift-ng
    "net.liftmodules"         %%  ("ng-js_"+liftEdition)              % "0.2_1.3.5"           % "compile", // https://github.com/joescii/lift-ng-js
    "org.eclipse.jetty"       %   "jetty-webapp"                      % "8.1.7.v20120910"     % "container,test",
    "org.eclipse.jetty.orbit" %   "javax.servlet"                     % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          %   "logback-classic"                   % "1.0.6"               % "compile",
    "org.scalatest"           %%  "scalatest"                         % "2.2.1"               % "test->*",
    "org.seleniumhq.selenium" %   "selenium-java"                     % "2.43.1"              % "test"
  )
}

(Keys.test in Test) <<= (Keys.test in Test) dependsOn (start in container.Configuration)

(Keys.testOnly in Test) <<= (Keys.testOnly in Test) dependsOn (start in container.Configuration)

(Keys.testQuick in Test) <<= (Keys.testOnly in Test) dependsOn (start in container.Configuration)

parallelExecution in Test := false

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](version, liftVersion, scalaVersion)

buildInfoPackage := "net.liftmodules.ng.test"

