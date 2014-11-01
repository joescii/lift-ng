package net.liftmodules.ng.test

class EmbeddedFuturesSpec extends BaseSpec {
  "The futures page" should "load" in {
    initialize("embedded-futures")
    click on "go-button"
  }

  "The future field that resolves before sending" should "be set to 'resolved'" in {
    eventually { id("resolved-output").element.text should be ("resolved") }
  }

  "The future field that fails" should "be set to 'failed'" in {
    eventually { id("failed-output").element.text should be ("failed") }
  }

  "The future field that contains a string" should "be set to 'future'" in {
    eventually { id("string-output").element.text should be ("future") }
  }

  "The future field that contains an object" should "set its two fields" in {
    eventually {
      id("object-str-output").element.text should be ("string")
      id("object-num-output").element.text should be ("42")
    }
  }

  "The future field that contains an array of futures" should "set the two string fields" in {
    eventually {
      id("object-arr0-output").element.text should be ("Roll")
      id("object-arr1-output").element.text should be ("Tide!")
    }
  }

  "The future field that contains an object with its own embedded futures" should "set all of its fields" in {
    eventually {
      id("object-fobj-resolved-output").element.text should be ("sub resolved")
      id("object-fobj-failed-output").element.text should be ("sub fail")
      id("object-fobj-string-output").element.text should be ("sub string")
      id("object-fobj-obj-str-output").element.text should be ("sub obj string")
      id("object-fobj-obj-num-output").element.text should be ("44")
    }
  }



}
