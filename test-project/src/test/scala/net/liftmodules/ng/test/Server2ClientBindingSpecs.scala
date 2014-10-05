package net.liftmodules.ng.test

trait Server2ClientBindingSpecs extends BaseSpec {
  def tests = {
    "The counter output" should "initially be zero" in {
      eventually { id("output").element.text should be ("0") }
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

      textField("input0").value should be ("test0")
      textField("input1").value should be ("test1")
    }

    "Refreshing the page" should "load all of the values from the server" in {
      // TODO: Get merge to work the way we want when arrays are involved.
      //    Thread.sleep(1500) // Give time for the binding to push to the server

      reloadPage()

      eventually {
        //      textField("input0").value should be ("test0")
        //      textField("input1").value should be ("test1")
        //      textField("input2").value should be ("test2")
        textField("input0").value should not be empty
        textField("input1").value should not be empty
        textField("input2").value should not be empty
      }
    }

    "The next button" should "load only the 4th input box" in {
      textField("input0").value = "test0"
      textField("input1").value = "test1"
      textField("input2").value = "test2"

      click on "button-next"
      eventually { textField("input3").value should not be empty }

      textField("input0").value should be ("test0")
      textField("input1").value should be ("test1")
      textField("input2").value should be ("test2")
    }

    "The button on the page" should "trigger a counter" in {
      click on "button"
      eventually { id("output").element.text should be ("1") }
      eventually { id("output").element.text should be ("2") }
    }
  }
}

class Server2ClientSessionBindingSpecs extends Server2ClientBindingSpecs {
  "The Server To Client Session Binding Tests page" should "load" in {
    go to s"$index/server2ClientSessionBind"
    eventually { pageTitle should be ("App: Server 2 Client Session Binding") }
  }

  tests

  "Reloading the page" should "NOT restart the counter" in {
    reloadPage()

    eventually { id("output").element.text.toInt should not be (0) }
  }
}

class Server2ClientRequestBindingSpecs extends Server2ClientBindingSpecs {
  "The Server To Client Request Binding Tests page" should "load" in {
    go to s"$index/server2ClientRequestBind"
    eventually { pageTitle should be ("App: Server 2 Client Request Binding") }
  }

  tests

  "Reloading the page" should "restart the counter" in {
    reloadPage()

    eventually { id("output").element.text should be ("0") }
  }
}
