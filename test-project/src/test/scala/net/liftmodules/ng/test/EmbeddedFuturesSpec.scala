package net.liftmodules.ng.test

class EmbeddedFuturesSpec extends BaseSpec {
  "The embedded futures page" should "load" in {
    initialize("embedded-futures")
    click on "go-button"
  }

  "The future service field that resolves before sending" should "be set to 'resolved'" in {
    eventually { id("svc-resolved-output").element.text should be ("resolved") }
  }

  "The future service field that fails" should "be set to 'failed'" in {
    eventually { id("svc-failed-output").element.text should be ("failed") }
  }

  "The future service field that contains a string" should "be set to 'future'" in {
    eventually { id("svc-string-output").element.text should be ("future") }
  }

  "The future service field that contains an object" should "set its two fields" in {
    eventually {
      id("svc-object-str-output").element.text should be ("string")
      id("svc-object-num-output").element.text should be ("42")
    }
  }

  "The future service field that contains an array of futures" should "set the two string fields" in {
    eventually {
      id("svc-object-arr0-output").element.text should be ("Roll")
      id("svc-object-arr1-output").element.text should be ("Tide!")
    }
  }

  "The future service field that contains an object with its own embedded futures" should "set all of its fields" in {
    eventually {
      id("svc-object-fobj-resolved-output").element.text should be ("sub resolved")
      id("svc-object-fobj-failed-output").element.text should be ("sub fail")
      id("svc-object-fobj-string-output").element.text should be ("sub string")
      id("svc-object-fobj-obj-str-output").element.text should be ("sub obj string")
      id("svc-object-fobj-obj-num-output").element.text should be ("44")
    }
  }


  "The future event field that resolves before sending" should "be set to 'resolved'" in {
    eventually { id("event-resolved-output").element.text should be ("resolved") }
  }

  "The future event field that fails" should "be set to 'failed'" in {
    eventually { id("event-failed-output").element.text should be ("failed") }
  }

  "The future event field that contains a string" should "be set to 'future'" in {
    eventually { id("event-string-output").element.text should be ("future") }
  }

  "The future event field that contains an object" should "set its two fields" in {
    eventually {
      id("event-object-str-output").element.text should be ("string")
      id("event-object-num-output").element.text should be ("42")
    }
  }

  "The future event field that contains an array of futures" should "set the two string fields" in {
    eventually {
      id("event-object-arr0-output").element.text should be ("Roll")
      id("event-object-arr1-output").element.text should be ("Tide!")
    }
  }

  "The future event field that contains an object with its own embedded futures" should "set all of its fields" in {
    eventually {
      id("event-object-fobj-resolved-output").element.text should be ("sub resolved")
      id("event-object-fobj-failed-output").element.text should be ("sub fail")
      id("event-object-fobj-string-output").element.text should be ("sub string")
      id("event-object-fobj-obj-str-output").element.text should be ("sub obj string")
      id("event-object-fobj-obj-num-output").element.text should be ("44")
    }
  }


  "The future binding field that resolves before sending" should "be set to 'resolved'" in {
    eventually { id("binding-resolved-output").element.text should be ("resolved") }
  }

  "The future binding field that fails" should "be set to 'failed'" in {
    eventually { id("binding-failed-output").element.text should be ("failed") }
  }

  "The future binding field that contains a string" should "be set to 'future'" in {
    eventually { id("binding-string-output").element.text should be ("future") }
  }

  "The future binding field that contains an object" should "set its two fields" in {
    eventually {
      id("binding-object-str-output").element.text should be ("string")
      id("binding-object-num-output").element.text should be ("42")
    }
  }

  "The future binding field that contains an array of futures" should "set the two string fields" in {
    eventually {
      id("binding-object-arr0-output").element.text should be ("Roll")
      id("binding-object-arr1-output").element.text should be ("Tide!")
    }
  }

  "The future binding field that contains an object with its own embedded futures" should "set all of its fields" in {
    eventually {
      id("binding-object-fobj-resolved-output").element.text should be ("sub resolved")
      id("binding-object-fobj-failed-output").element.text should be ("sub fail")
      id("binding-object-fobj-string-output").element.text should be ("sub string")
      id("binding-object-fobj-obj-str-output").element.text should be ("sub obj string")
      id("binding-object-fobj-obj-num-output").element.text should be ("44")
    }
  }


  "The scala future field that resolves before sending" should "be set to 'resolved'" in {
    eventually { id("scala-resolved-output").element.text should be ("resolved") }
  }

  "The scala future field that fails" should "be set to 'failed'" in {
    eventually { id("scala-failed-output").element.text should be ("failed") }
  }

  "The scala future field that contains a string" should "be set to 'future'" in {
    eventually { id("scala-string-output").element.text should be ("future") }
  }

  "The scala future field that contains an object" should "set its two fields" in {
    eventually {
      id("scala-object-str-output").element.text should be ("string")
      id("scala-object-num-output").element.text should be ("42")
    }
  }

}
