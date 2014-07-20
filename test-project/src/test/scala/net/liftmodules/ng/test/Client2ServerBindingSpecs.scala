package net.liftmodules.ng.test

class Client2ServerBindingSpecs extends BaseSpec {
  "The Server To Client Binding Tests page" should "load" in {
    go to s"$index/client2ServerBind"
    eventually { pageTitle should be ("App: Client 2 Server Binding") }
  }

  "The input text" should "be reflected in the output text" in {
    textField("inputTxt").value = "a"
    eventually { id("output").element.text should be ("a") }
    textField("inputTxt").value = "ab"
    eventually { id("output").element.text should be ("ab") }
    textField("inputTxt").value = "abc"
    eventually { id("output").element.text should be ("abc") }
  }
}
