package net.liftmodules.ng.test

class StaticServiceSpecs extends BaseSpec{
  "The static services page" should "load" in {
    go to s"$index/static"
    eventually { pageTitle should be ("App: Static") }
  }

  "The static string" should "be 'FromServer1'" in {
    id("outputStr").element.text should be ("FromServer1")
  }

  "The static int" should "be 42" in {
    id("outputInt").element.text should be ("42")
  }

  "The static string field" should "be 'FromServer2'" in {
    id("outputStrField").element.text should be ("FromServer2")
  }

  "The static int field" should "be 88" in {
    id("outputIntField").element.text should be ("88")
  }

}
