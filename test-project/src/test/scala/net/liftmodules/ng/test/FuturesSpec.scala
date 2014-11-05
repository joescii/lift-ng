package net.liftmodules.ng.test

class FuturesSpec extends BaseSpec {
  "The futures page" should "load" in {
    initialize("futures")
  }

  "The angular service with no arguments" should "send 'FromFuture' up to the client after roughly 1 second" in {
    click on "no-arg-button"
    eventually{id("no-arg-output").element.text should be ("FromFuture")}
  }

  "The angular service with no arguments which fails" should "send the failure message 'FailureTest' up to the client" in {
    click on "failure-button"
    eventually{id("failure-output").element.text should be ("FailureTest")}
  }

  "The angular service with a string arg" should "send 'FromFuture: arg' up to the client" in {
    textField("string-input").value = "arg"
    click on "string-button"
    eventually(id("string-output").element.text should be ("FromFuture: arg"))
  }

  "The angular service with a JSON argument" should "send both text box strings to the server and eventually populate the test " +
    "text boxes with 'FromFuture argA' and 'FromFuture argB'" in {
    textField("json-input-a").value = "argA"
    textField("json-input-b").value = "argB"
    click on "json-button"
    eventually {
      id("json-output-a").element.text should be ("FromFuture argA")
      id("json-output-b").element.text should be ("FromFuture argB")
    }
  }

  "The angular service which returns an Empty" should "resolve its promise after roughly 1 second" in {
    click on "empty-button"
    eventually(id("empty-output").element.text should be ("returned"))
  }

  "The angular service which is satisfied immediately" should "resolve its promise" in {
    click on "satisfied-button"
    eventually(id("satisfied-output").element.text should be ("satisfied"))
  }

  "The angular service with a Scala Future" should "send 'ScalaFuture' up to the client after roughly 1 second" in {
    click on "scala-future-button"
    eventually{id("scala-future-output").element.text should be ("ScalaFuture")}
  }


  "The futures page" should "reload" in {
    initialize("futures")
  }

  "The angular services" should "correctly load concurrently" in {
    click on "no-arg-button"
    click on "failure-button"
    textField("string-input").value = "arg"
    click on "string-button"
    textField("json-input-a").value = "argA"
    textField("json-input-b").value = "argB"
    click on "json-button"
    click on "empty-button"
    click on "satisfied-button"
    click on "scala-future-button"
    eventually{
      id("no-arg-output").element.text should be ("FromFuture")
      id("failure-output").element.text should be ("FailureTest")
      id("string-output").element.text should be ("FromFuture: arg")
      id("json-output-a").element.text should be ("FromFuture argA")
      id("json-output-b").element.text should be ("FromFuture argB")
      id("empty-output").element.text should be ("returned")
      id("satisfied-output").element.text should be ("satisfied")
      id("scala-future-output").element.text should be ("ScalaFuture")
    }
  }

}
