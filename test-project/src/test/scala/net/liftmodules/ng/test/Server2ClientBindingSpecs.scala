package net.liftmodules.ng.test


class Server2ClientSessionBindingSpecs extends BaseSpec {
  "The Server To Client Session Binding Tests page" should "load" in {
    initialize("server2ClientSessionBind")
  }

  "The counter output" should "initially be zero" in {
    eventually { id("output").element.text should be ("0") }
  }

  "The button on the page" should "trigger a counter" in {
    click on "button"
    eventually { id("output").element.text should be ("1") }
    eventually { id("output").element.text should be ("2") }
  }

  "Reloading the page" should "NOT restart the counter" in {
    reloadPage()

    eventually { id("output").element.text.toInt should not be (0) }
  }
}

class Server2ClientOptimizedBindingSpecs extends BaseSpec {
  "The Server To Client Optimized Binding Tests page" should "load" in {
    initialize("server2ClientOptimizedBind")
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

}

class Server2ClientStandardBindingSpecs extends BaseSpec {
  "The Server To Client Standard Binding Tests page" should "load" in {
    initialize("server2ClientStandardBind")
  }

  "The next button" should "load the next box while clobbering the others" in {
    click on "button-next"
    eventually { textField("input0").value should not be empty }
    textField("input0").value = "test0"

    click on "button-next"
    eventually { textField("input1").value should not be empty }
    textField("input1").value = "test1"

    click on "button-next"
    eventually { textField("input2").value should not be empty }

    textField("input0").value should not be ("test0")
    textField("input1").value should not be ("test1")
  }

  "Refreshing the page" should "load all of the values from the server" in {
    reloadPage()

    eventually {
      textField("input0").value should not be empty
      textField("input1").value should not be empty
      textField("input2").value should not be empty
    }
  }

  "The next button" should "load every input box" in {
    textField("input0").value = "test0"
    textField("input1").value = "test1"
    textField("input2").value = "test2"

    click on "button-next"
    eventually { textField("input3").value should not be empty }

    textField("input0").value should not be ("test0")
    textField("input1").value should not be ("test1")
    textField("input2").value should not be ("test2")
  }

}

class Server2ClientRequestBindingSpecs extends BaseSpec {
  "The Server To Client Request Binding Tests page" should "load" in {
    initialize("server2ClientRequestBind")
  }

  "The counter" should "increment" in {
    eventually { id("output").element.text should be ("1") }
    eventually { id("output").element.text should be ("2") }
  }

  "Reloading the page" should "restart the counter" in {
    reloadPage()

    eventually { id("output").element.text should be ("0") }
  }
}

class MultipleBinderSpecs extends BaseSpec {
  "Multiple Server to Client Binders page" should "load" in {
    initialize("multipleBinders")
  }

  "The first binder" should "should be set to Bound-1" in {
    eventually {
      id("binder1").element.text should be("Bound-1")
    }
  }

  "The second binder" should "should be set to Bound-2" in {
    eventually {
      id("binder2").element.text should be("Bound-2")
    }
  }
}