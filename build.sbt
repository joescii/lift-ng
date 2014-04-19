name := "ng"

organization := "net.liftmodules"

version := "0.3.0-SNAPSHOT"

liftVersion <<= liftVersion ?? "2.5.1"

liftEdition <<= liftVersion { _.substring(0,3) }

name <<= (name, liftEdition) { (n, e) =>  n + "_" + e }

scalaVersion <<= scalaVersion ?? "2.9.1"  // This project's scala version is purposefully set at the lowest common denominator to ensure each version compiles.

crossScalaVersions := Seq("2.10.3", "2.9.2", "2.9.1-1", "2.9.1")

resolvers += "CB Central Mirror" at "http://repo.cloudbees.com/content/groups/public"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies <++= liftVersion { v =>
  Seq(
    "net.liftweb"   %% "lift-webkit"  % v  % "provided"
  )
}

scalacOptions <<= scalaVersion map { v: String =>
  val opts = "-deprecation" :: "-unchecked" :: Nil
  if (v.startsWith("2.9.")) opts else opts ++ ("-feature" :: "-language:postfixOps" :: Nil)
}

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
        <url>https://github.com/barnesjd/lift-ng</url>
        <licenses>
            <license>
              <name>Apache 2.0 License</name>
              <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
              <distribution>repo</distribution>
            </license>
         </licenses>
         <scm>
            <url>git@github.com:barnesjd/lift-ng.git</url>
            <connection>scm:git:git@github.com:barnesjd/lift-ng.git</connection>
         </scm>
         <developers>
            <developer>
              <id>htmldoug</id>
              <name>Doug Roper</name>
              <url>https://github.com/htmldoug</url>
            </developer>
            <developer>
              <id>barnesjd</id>
              <name>Joe Barnes</name>
              <url>https://github.com/barnesjd</url>
            </developer>
         </developers>
 )
