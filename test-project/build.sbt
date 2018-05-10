name := "ng-test"

organization := "net.liftmodules"

version := "0.12.0-RC1"

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

unmanagedResourceDirectories in Test += baseDirectory.value / "src/main/webapp"

def clusteringSupported(liftEdition: String, scalaBinaryVersion: String): Boolean = liftEdition == "3.2" && scalaBinaryVersion == "2.11"

libraryDependencies ++= {
  val lift = liftVersion.value
  val liftng = version.value
  val edition = liftEdition.value
  val jqEdition = if(edition == "3.2") "3.1" else edition
  val clusterVer = "0.0.2"
  val clustering = if(clusteringSupported(edition, scalaBinaryVersion.value)) Seq(
    "net.liftmodules" %% ("lift-cluster-jetty9_"+edition) % clusterVer % "compile",
    "net.liftmodules" %% ("lift-cluster-kryo_"+edition) % clusterVer % "compile",
    "com.h2database" % "h2" % "1.4.197"
  ) else Seq() // https://github.com/joescii/lift-cluster

  Seq(
    "net.liftweb"             %%  "lift-webkit"                       % lift                  % "compile",
    "net.liftmodules"         %%  ("lift-jquery-module_"+jqEdition)   % "2.10"                % "compile",
    "net.liftmodules"         %%  ("ng_"+edition)                     % liftng                % "compile", // https://github.com/joescii/lift-ng
    "org.webjars"             %   "angularjs"                         % "1.4.8",
    "org.eclipse.jetty"       %   "jetty-webapp"                      % "9.2.7.v20150116"     % "compile",
    "org.eclipse.jetty"       %   "jetty-plus"                        % "9.2.7.v20150116"     % "container,test", // For Jetty Config
    "org.eclipse.jetty.orbit" %   "javax.servlet"                     % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          %   "logback-classic"                   % "1.0.6"               % "compile",
    "org.scalatest"           %%  "scalatest"                         % "3.0.4"               % "test->*", // http://www.scalatest.org/
    "org.seleniumhq.selenium" %   "selenium-java"                     % "2.51.0"              % "test"     // http://www.seleniumhq.org/download/
  ) ++ clustering
}

excludeFilter in unmanagedSources := {
  HiddenFileFilter ||
    (if(clusteringSupported(liftEdition.value, scalaBinaryVersion.value)) "SerializationNoop.scala" else "SerializationKryo.scala")
}

parallelExecution in Test := false

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](version, liftVersion, scalaVersion)
buildInfoPackage := "net.liftmodules.ng.test"

enablePlugins(JettyPlugin)
(runMain in Compile) := (runMain in Compile).dependsOn(webappPrepare).evaluated
(bgRun in Compile) := (bgRun in Compile).dependsOn(webappPrepare).evaluated

