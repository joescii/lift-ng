resolvers += Resolver.url("untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.2")

addSbtPlugin("com.untyped" % "sbt-js" % "0.8-M3") // https://github.com/untyped/sbt-plugins

addSbtPlugin("com.joescii" % "sbt-jasmine-plugin" % "1.3.0")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")
