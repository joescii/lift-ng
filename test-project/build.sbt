name := "ng-test"

organization := "net.liftmodules"

version := "0.5.0"

liftVersion <<= liftVersion ?? "2.6-RC1"

liftEdition <<= liftVersion { _.substring(0,3) }

resolvers ++= Seq(
  "staging"   at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-deprecation", "-unchecked")


libraryDependencies <++= (liftVersion, version) { (lift, liftng) =>
  val liftEdition = lift.substring(0,3)
  Seq(
    "net.liftweb"             %%  "lift-webkit"                       % lift                  % "compile",
    "net.liftmodules"         %%  ("lift-jquery-module_"+liftEdition) % "2.8"                 % "compile",
    "net.liftmodules"         %%  ("ng_"+liftEdition)                 % liftng                % "compile", // https://github.com/joescii/lift-ng
    "net.liftmodules"         %%  ("ng-js_"+liftEdition)              % "0.1_1.2.22"          % "compile", // https://github.com/joescii/lift-ng-js
    "org.eclipse.jetty"       %   "jetty-webapp"                      % "8.1.7.v20120910"     % "container,test",
    "org.eclipse.jetty.orbit" %   "javax.servlet"                     % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          %   "logback-classic"                   % "1.0.6"               % "compile",
    "org.scalatest"           %%  "scalatest"                         % "2.2.1"               % "test->*",
    "org.seleniumhq.selenium" %   "selenium-java"                     % "2.39.0"              % "test"
  )
}

(Keys.test in Test) <<= (Keys.test in Test) dependsOn (start in container.Configuration)

(Keys.testOnly in Test) <<= (Keys.testOnly in Test) dependsOn (start in container.Configuration)

parallelExecution in Test := false

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](version)

buildInfoPackage := "net.liftmodules.ng.test"

