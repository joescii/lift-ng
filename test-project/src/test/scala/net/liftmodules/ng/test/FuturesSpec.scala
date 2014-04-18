package net.liftmodules.ng.test

class FuturesSpec extends BaseSpec {
  "The futures page" should "load" in {
    go to s"$index/futures"
    eventually { pageTitle should be ("App: Futures") }
  }

  "The angular service with a JSON argument" should "send both text box strings to the server and eventually populate the test " +
    "text boxes with 'FromFuture client1' and 'FromFuture client2'" in {
    textField("inputA").value = "client1"
    textField("inputB").value = "client2"
    click on "button"
    eventually {
      id("outputA").element.text should be ("FromFuture client1")
      id("outputB").element.text should be ("FromFuture client2")
    }
  }

}
