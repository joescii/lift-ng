import sbt._
import sbt.Keys._

object LiftNgBuild extends Build {

  val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")

  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")

  val commonSettings = Seq(
    scalaVersion := "2.9.1",  // This project's scala version is purposefully set at the lowest common denominator to ensure each version compiles.
    organization := "net.liftmodules",
    version := "0.1.2-SNAPSHOT"
  )
  
  lazy val project = Project(
    id = "lift-ng", 
    base = file("."),
    settings = Project.defaultSettings ++ commonSettings)

  lazy val testProject = Project(
    id = "lift-ng-test", 
    base = file("test-project"),
    settings = Project.defaultSettings ++ commonSettings) dependsOn(project)
}
