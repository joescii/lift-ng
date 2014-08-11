name := "ng"

organization := "net.liftmodules"

homepage := Some(url("https://github.com/joescii/lift-ng"))

version := "0.5.0-SNAPSHOT"

liftVersion <<= liftVersion ?? "2.5.1"

liftEdition <<= liftVersion { _.substring(0,3) }

name <<= (name, liftEdition) { (n, e) =>  n + "_" + e }

// Necessary beginning with sbt 0.13, otherwise Lift editions get messed up.
// E.g. "2.5" gets converted to "2-5"
moduleName := name.value

scalaVersion <<= scalaVersion ?? "2.9.1"  // This project's scala version is purposefully set at the lowest common denominator to ensure each version compiles.

crossScalaVersions := Seq("2.10.4", "2.9.2", "2.9.1")

resolvers += "CB Central Mirror" at "http://repo.cloudbees.com/content/groups/public"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies <++= liftVersion { v =>
  Seq(
    "net.liftweb"   %% "lift-webkit"  % v       % "provided",
    "com.joescii"   %  "j2js-i18n"    % "0.1"   % "compile",
    "org.scalaz"    %% "scalaz-core"  % "6.0.4" % "compile",  // Ideally, keep this in sync with https://github.com/lift/framework/blob/master/project/Dependencies.scala#L32
    "org.scalatest" %% "scalatest"    % "1.9.2" % "test"
  )
}

scalacOptions <<= scalaVersion map { v: String =>
  val opts = "-deprecation" :: "-unchecked" :: Nil
  if (v.startsWith("2.9.")) opts else opts ++ ("-feature" :: "-language:postfixOps" :: "-language:implicitConversions" :: Nil)
}

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](version)

buildInfoPackage := "net.liftmodules.ng"

publishTo <<= version { _.endsWith("SNAPSHOT") match {
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

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("lift", "angular")

(description in LsKeys.lsync) := "lift-ng is the most powerful, most secure AngularJS backend available today"

(LsKeys.ghUser in LsKeys.lsync) := Some("joescii")

(LsKeys.ghRepo in LsKeys.lsync) := Some("lift-ng")

(LsKeys.ghBranch in LsKeys.lsync) := Some("master")


// Jasmine stuff
seq(jasmineSettings : _*)

appJsDir <+= sourceDirectory { src => src / "main" / "js" }

appJsLibDir <+= sourceDirectory { src => src / "test" / "js" / "3rdlib" }

jasmineTestDir <+= sourceDirectory { src => src /  "test" / "js" }

jasmineConfFile <+= sourceDirectory { src => src / "test" / "js" / "3rdlib" / "test.dependencies.js" }

jasmineRequireJsFile <+= sourceDirectory { src => src / "test" / "js" / "3rdlib" / "require" / "require-2.0.6.js" }

jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "js" / "3rdlib" / "require.conf.js" }

(Keys.test in Test) <<= (Keys.test in Test) dependsOn (jasmine)
