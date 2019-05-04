import sbt._

object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn(yuiCompressorPlugin)
  lazy val yuiCompressorPlugin = uri("git://github.com/indrajitr/sbt-yui-compressor.git#89304ec0c988183d1f1a889e665e0269fe513031")
}