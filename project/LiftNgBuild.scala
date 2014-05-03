import sbt._
import sbt.Keys._

object LiftNgBuild extends Build {

  val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")

  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")
  
  val copyJs = resourceGenerators in Compile <+= (sourceDirectory, resourceManaged in Compile, version) map { (src, rsrc, ver) =>
    val jsSrcDir = src / "main" / "js"
    val jsDstDir = rsrc
    val jsSrcs = IO.listFiles(jsSrcDir)
    val srcRelNames = jsSrcs.map(_.getPath.substring(jsSrcDir.getPath.length+1))
    val withVer = srcRelNames.map(n => n.substring(0, n.length-3)+"-"+ver+".js")
    val dstRelNames = withVer.map("toserve/net/liftmodules/ng/js/" + _)
    val jsDsts = dstRelNames.map(new File(jsDstDir, _))
    (jsSrcs zip jsDsts) map { case (src, dst) => IO.copyFile(src, dst) }
    jsDsts.toSeq
  }
  
  val liftngSettings = Project.defaultSettings ++ Seq(
    copyJs
  )
  
  lazy val project = Project(
    id = "lift-ng", 
    base = file("."),
    settings = liftngSettings
  )
}
