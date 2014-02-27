import sbt._
import sbt.Keys._

object LiftNgBuild extends Build {

  val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")

  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")

  val commonSettings = Seq(
    organization := "net.liftmodules",
    version := "0.1.2-SNAPSHOT"
  )
  
  lazy val project = Project(
    id = "lift-ng", 
    base = file("."),
    settings = Project.defaultSettings ++ commonSettings)

  // Purposefully not setting this as a dependency.  While that would be convenient, it doesn't work
  // because the test project is in Scala 2.10.3 so we can use ScalaTest 2.0 which has Selenium
  lazy val testProject = Project(
    id = "lift-ng-test", 
    base = file("test-project"),
    settings = Project.defaultSettings ++ commonSettings)
}
