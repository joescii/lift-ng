package net.liftmodules.ng.test

class Client2ServerBindingSpecs extends BaseSpec {
  "The Server To Client Binding Tests page" should "load" in {
    go to s"$index/client2ServerBind"
    eventually { pageTitle should be ("App: Client 2 Server Binding") }
  }

  "The input text" should "be reflected in the output text" in {
    eventually { id("output").element.text should be ("") }
    textField("input").value = "a"
    eventually { id("output").element.text should be ("a") }
    textField("input").value = "ab"
    eventually { id("output").element.text should be ("ab") }
    textField("input").value = "abc"
    eventually { id("output").element.text should be ("abc") }
  }

  "The input" should "persist over a reload of the page" in {
    reloadPage()
    eventually { textField("input").value should be ("abc") }
  }
}
