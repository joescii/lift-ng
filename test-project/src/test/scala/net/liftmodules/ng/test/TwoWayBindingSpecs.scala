package net.liftmodules.ng.test

trait TwoWayBindingSpecs extends BaseSpec {
  def tests = "The server" should "increment the count" in {
    eventually(textField("counter").value should be ("0"))
    textField("counter").value = "1"
    eventually(textField("counter").value should be ("2"))
    textField("counter").value = "12"
    eventually(textField("counter").value should be ("13"))
    textField("counter").value = "99"
    eventually(textField("counter").value should be ("100"))
  }
}

class TwoWaySessionBindingSpecs extends TwoWayBindingSpecs {
  "The 2-way session binding page" should "load" in {
    initialize("twoWaySessionBinding")
  }

  tests

  "A reload" should "not reset the counter" in {
    reloadPage()
    eventually(textField("counter").value should be ("100"))
  }
}

class TwoWayRequestBindingSpecs extends TwoWayBindingSpecs {
  "The 2-way request binding page" should "load" in {
    initialize("twoWayRequestBinding")
  }

  tests

  "A reload" should "reset the counter" in {
    reloadPage()
    eventually(textField("counter").value should be ("0"))
  }
}
