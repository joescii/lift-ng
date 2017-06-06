resolvers += Resolver.bintrayIvyRepo("untyped", "ivy")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.2")

addSbtPlugin("com.untyped" % "sbt-js" % "0.8") // https://github.com/untyped/sbt-plugins

addSbtPlugin("com.joescii" % "sbt-jasmine-plugin" % "1.3.0")

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC3")
