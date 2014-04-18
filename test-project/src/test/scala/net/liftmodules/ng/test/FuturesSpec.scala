package net.liftmodules.ng.test

class FuturesSpec extends BaseSpec {
  "The futures page" should "load" in {
    go to s"$index/futures"
    eventually { pageTitle should be ("App: Futures") }
  }


}
