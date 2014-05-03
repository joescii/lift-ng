libraryDependencies <+= sbtVersion(v => v match {
case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
case "0.11.3" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
case x if (x.startsWith("0.12")) => "com.github.siasia" %% "xsbt-web-plugin" % "0.12.0-0.2.11.1"
})

//Enable the sbt idea plugin
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

//Enable the sbt eclipse plugin
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.2.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.5")
