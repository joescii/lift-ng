package net.liftmodules.ng.test

class Server2ClientBindingSpecs extends BaseSpec {
  "The Actors - Server To Client Binding Tests page" should "load" in {
    go to s"$index/server2ClientBind"
    eventually { pageTitle should be ("App: Server 2 Client Binding") }
  }

  "The button on the page" should "trigger a counter" in {
    click on "button"
    eventually { id("output").element.text should be ("0") }
    eventually { id("output").element.text should be ("1") }
    eventually { id("output").element.text should be ("2") }
  }
}
