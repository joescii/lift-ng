package net.liftmodules.ng.test

class WebjarsSpecs extends BaseSpec {
  "The static page" should "load" in {
    initialize("static")
  }

  "The angular resource module" should "be on the static page" in {
    id("angular-resource_js").element
  }

  "The angular touch module" should "be on the static page" in {
    id("angular-touch_js").element
  }

  "The snippets page" should "load" in {
    initialize("snippets")
  }

  "The angular loader module" should "be on the snippets page" in {
    id("angular-loader_js").element
  }
}
