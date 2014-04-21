package net.liftmodules.ng.test

class FuturesSpec extends BaseSpec {
  "The futures page" should "load" in {
    go to s"$index/futures"
    eventually { pageTitle should be ("App: Futures") }
  }

  "The angular service with no arguments" should "send 'FromFuture' up to the client after roughly 1 second" in {
    click on "no-arg-button"
    eventually{id("no-arg-output").element.text should be ("FromFuture")}
  }

  "The angular service with no arguments which fails" should "send the failure message 'FailureTest' up to the client" in {
    click on "failure-button"
    eventually{id("failure-output").element.text should be ("FailureTest")}
  }

  "The angular service with a JSON argument" should "send both text box strings to the server and eventually populate the test " +
    "text boxes with 'FromFuture client1' and 'FromFuture client2'" ignore {
    textField("inputA").value = "client1"
    textField("inputB").value = "client2"
    click on "button"
    eventually {
      id("outputA").element.text should be ("FromFuture client1")
      id("outputB").element.text should be ("FromFuture client2")
    }
  }

}
