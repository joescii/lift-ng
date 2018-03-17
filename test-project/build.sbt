name := "ng-test"

organization := "net.liftmodules"

version := "0.11.1-SNAPSHOT"

val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")
val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")
liftVersion := System.getProperty("lift.version", "3.2.0")
liftEdition := liftVersion.value.substring(0,3)

scalaVersion := System.getProperty("scala.version", "2.11.12")

scalacOptions ++= Seq("-deprecation", "-unchecked")

resolvers ++= Seq(
  "staging"   at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "https://oss.sonatype.org/content/repositories/releases"
)

enablePlugins(ContainerPlugin)

unmanagedResourceDirectories in Test += baseDirectory.value / "src/main/webapp"

def clusteringSupported(liftEdition: String, scalaBinaryVersion: String): Boolean = liftEdition == "3.2" && scalaBinaryVersion == "2.11"

def dependencies(version: String, liftVersion: String, liftEdition: String, scalaBinaryVersion: String) = {
  val lift = liftVersion
  val liftng = version
  val edition = liftEdition
  val jqEdition = if(edition == "3.2") "3.1" else edition
  val kryo = if(clusteringSupported(edition, scalaBinaryVersion)) Seq("net.liftmodules" %% ("lift-cluster-kryo_"+edition) % "0.0.2" % "compile") else Seq() // https://github.com/joescii/lift-cluster

  Seq(
    "net.liftweb"             %%  "lift-webkit"                       % lift                  % "compile",
    "net.liftmodules"         %%  ("lift-jquery-module_"+jqEdition)   % "2.10"                % "compile",
    "net.liftmodules"         %%  ("ng_"+edition)                     % liftng                % "compile", // https://github.com/joescii/lift-ng
    "net.liftmodules"         %% ("lift-cluster-jetty9_"+edition)  % "0.0.3-SNAPSHOT" ,
    "com.h2database" % "h2" % "1.4.196",
    "org.webjars"             %   "angularjs"                         % "1.4.8",
    "org.eclipse.jetty"       %   "jetty-webapp"                      % "9.2.7.v20150116"     % "compile",
    "org.eclipse.jetty"       %   "jetty-plus"                        % "9.2.7.v20150116"     % "container,test", // For Jetty Config
    "org.eclipse.jetty.orbit" %   "javax.servlet"                     % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          %   "logback-classic"                   % "1.0.6"               % "compile",
    "org.scalatest"           %%  "scalatest"                         % "3.0.4"               % "test->*", // http://www.scalatest.org/
    "org.seleniumhq.selenium" %   "selenium-java"                     % "2.51.0"              % "test"     // http://www.seleniumhq.org/download/
  ) ++ kryo
}

libraryDependencies := dependencies(version.value, liftVersion.value, liftEdition.value, scalaBinaryVersion.value)
containerLibs in Container := dependencies(version.value, liftVersion.value, liftEdition.value, scalaBinaryVersion.value)

containerLaunchCmd in Container :=
  { (port, path) => Seq("net.liftmodules.cluster.jetty9.Start", port.toString, path) }

excludeFilter in unmanagedSources := {
  HiddenFileFilter ||
    (if(clusteringSupported(liftEdition.value, scalaBinaryVersion.value)) "SerializationNoop.scala" else "SerializationKryo.scala")
}

(Keys.test in Test) := (Keys.test in Test).dependsOn (ContainerPlugin.start in Container).value
(Keys.testOnly in Test) := (Keys.testOnly in Test).dependsOn (ContainerPlugin.start in Container).evaluated
(Keys.testQuick in Test) := (Keys.testOnly in Test).dependsOn (ContainerPlugin.start in Container).evaluated

parallelExecution in Test := false

buildInfoSettings
sourceGenerators in Compile += buildInfo
buildInfoKeys := Seq[BuildInfoKey](version, liftVersion, scalaVersion)
buildInfoPackage := "net.liftmodules.ng.test"

