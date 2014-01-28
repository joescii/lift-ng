import sbt._
import sbt.Keys._

object LiftNgBuild extends Build {

  val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")

  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")

  lazy val project = Project("lift-ng", file("."))

  lazy val testProject = Project("lift-ng-test", file("test-project")) dependsOn(project)
}
