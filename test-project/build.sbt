name := "ng-test"

organization := "net.liftmodules"

version := "0.11.0"

val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")
val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")
liftVersion := System.getProperty("lift.version", "2.6.3")
liftEdition := liftVersion.value.substring(0,3)

scalaVersion := System.getProperty("scala.version", "2.11.12")

scalacOptions ++= Seq("-deprecation", "-unchecked")

resolvers ++= Seq(
  "staging"   at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "https://oss.sonatype.org/content/repositories/releases"
)

Seq(webSettings :_*)

unmanagedResourceDirectories in Test += baseDirectory.value / "src/main/webapp"

libraryDependencies ++= {
  val lift = liftVersion.value
  val liftng = version.value
  val liftEdition = lift.substring(0,3)
  val jqEdition = if(liftEdition equals "3.2") "3.1" else liftEdition
  Seq(
    "net.liftweb"             %%  "lift-webkit"                       % lift                  % "compile",
    "net.liftmodules"         %%  ("lift-jquery-module_"+jqEdition)   % "2.10"                % "compile",
    "net.liftmodules"         %%  ("ng_"+liftEdition)                 % liftng                % "compile", // https://github.com/joescii/lift-ng
    "org.webjars"             %   "angularjs"                         % "1.4.8",
    "org.eclipse.jetty"       %   "jetty-webapp"                      % "9.2.7.v20150116"     % "compile",
    "org.eclipse.jetty"       %   "jetty-plus"                        % "9.2.7.v20150116"     % "container,test", // For Jetty Config
    "org.eclipse.jetty.orbit" %   "javax.servlet"                     % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          %   "logback-classic"                   % "1.0.6"               % "compile",
    "org.scalatest"           %%  "scalatest"                         % "3.0.4"               % "test->*", // http://www.scalatest.org/
    "org.seleniumhq.selenium" %   "selenium-java"                     % "2.51.0"              % "test"     // http://www.seleniumhq.org/download/
  )
}

(Keys.test in Test) := (Keys.test in Test).dependsOn (start in container.Configuration).value
(Keys.testOnly in Test) := (Keys.testOnly in Test).dependsOn (start in container.Configuration).evaluated
(Keys.testQuick in Test) := (Keys.testOnly in Test).dependsOn (start in container.Configuration).evaluated

parallelExecution in Test := false

buildInfoSettings
sourceGenerators in Compile += buildInfo
buildInfoKeys := Seq[BuildInfoKey](version, liftVersion, scalaVersion)
buildInfoPackage := "net.liftmodules.ng.test"

