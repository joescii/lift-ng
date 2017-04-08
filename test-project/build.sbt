name := "ng-test"

organization := "net.liftmodules"

version := "0.9.4"

liftVersion := System.getProperty("lift.version", "2.6.3")

liftEdition <<= liftVersion { _.substring(0,3) }

scalaVersion := System.getProperty("scala.version", "2.11.8")

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
  val jqEdition = if(liftEdition startsWith "3") "3.0" else liftEdition
  val jq = if(liftEdition == "2.5") "2.8" else "2.9"
  Seq(
    "net.liftweb"             %%  "lift-webkit"                       % lift                  % "compile",
    "net.liftmodules"         %%  ("lift-jquery-module_"+jqEdition)   % jq                    % "compile",
    "net.liftmodules"         %%  ("ng_"+liftEdition)                 % liftng                % "compile", // https://github.com/joescii/lift-ng
    "org.webjars"             %   "angularjs"                         % "1.4.8",
//    "org.webjars.bower"             %   "angularjs"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-aria"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-animate"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-cookies"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-loader"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-messages"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-resource"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-route"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-sanitize"                         % "1.4.7",
//    "org.webjars.bower"             %   "angular-touch"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-aria"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-animate"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-cookies"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-loader"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-messages"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-resource"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-route"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-sanitize"                         % "1.4.7",
//    "org.webjars.npm"             %   "angular-touch"                         % "1.4.7",
    "org.eclipse.jetty"       %   "jetty-webapp"                      % "9.2.7.v20150116"     % "compile",
    "org.eclipse.jetty"       %   "jetty-plus"                        % "9.2.7.v20150116"     % "container,test", // For Jetty Config
    "org.eclipse.jetty.orbit" %   "javax.servlet"                     % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          %   "logback-classic"                   % "1.0.6"               % "compile",
    "org.scalatest"           %%  "scalatest"                         % "2.2.4"               % "test->*", // http://www.scalatest.org/
    "org.seleniumhq.selenium" %   "selenium-java"                     % "2.51.0"              % "test"     // http://www.seleniumhq.org/download/
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

