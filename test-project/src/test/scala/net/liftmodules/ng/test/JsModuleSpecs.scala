package net.liftmodules.ng.test

class JsModuleSpecs extends BaseSpec {
  "The JsModules/head.js page" should "load" in {
    initialize("head-js")
  }

  "The version" should "match" in {
    id("version").element.text should be (BuildInfo.version)
  }

  "The path" should "match" in {
    id("path").element.text should be ("/classpath/net/liftmodules/ng/js/liftproxy-"+BuildInfo.version+".js")
  }
}
