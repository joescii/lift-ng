package net.liftmodules.ng.test

class TwoWayBindingSpecs extends BaseSpec {
  "The 2-way binding page" should "load" in {
    go to s"$index/twoWayBinding"
    eventually { pageTitle should be ("App: 2-way Binding") }
  }

}
