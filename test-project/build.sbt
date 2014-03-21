name := "ng-test"

organization := "net.liftmodules"

version := "0.2.2"

liftVersion <<= liftVersion ?? "2.5.1"

liftEdition <<= liftVersion { _.substring(0,3) }

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-deprecation", "-unchecked")


libraryDependencies <++= version { ver =>
  val liftVersion = "2.5.1"
  val liftEdition = "2.5"
  Seq(
    "net.liftweb"             %%  "lift-webkit"                       % liftVersion           % "compile",
    "net.liftmodules"         %%  ("lift-jquery-module_"+liftEdition) % "2.4"                 % "compile",
    "net.liftmodules"         %%  ("ng_"+liftEdition)                 % ver                   % "compile", // https://github.com/barnesjd/lift-ng
    "org.eclipse.jetty"       %   "jetty-webapp"                      % "8.1.7.v20120910"     % "container,test",
    "org.eclipse.jetty.orbit" %   "javax.servlet"                     % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          %   "logback-classic"                   % "1.0.6"               % "compile",
    "org.scalatest"           %%  "scalatest"                         % "2.0"                 % "test->*",
    "org.seleniumhq.selenium" %   "selenium-java"                     % "2.39.0"              % "test"
  )
}

(test in Test) <<= (test in Test) dependsOn (start in container.Configuration)

parallelExecution in Test := false
