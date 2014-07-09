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

  "The next button" should "load the next box without impacting the others" in {
    click on "button-next"
    eventually { textField("input0").value should not be empty }
    textField("input0").value = "test0"

    click on "button-next"
    eventually { textField("input1").value should not be empty }
    textField("input1").value = "test1"

    click on "button-next"
    eventually { textField("input2").value should not be empty }
    textField("input2").value = "test2"

    click on "button-next"
    eventually { textField("input3").value should not be empty }

    textField("input0").value should be ("test0")
    textField("input1").value should be ("test1")
    textField("input2").value should be ("test2")
  }
}
