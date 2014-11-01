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

  "The future field that contains an object with its own embedded futures" should "set its num and str fields" in {
    eventually {
      id("object-future-str-output").element.text should be ("string")
      id("object-future-num-output").element.text should be ("43")
      id("object-future-fail-output").element.text should be ("failed2")
    }
  }

  "The future field that contains an object with its own embedded futures" should "set the two fields from the embedded future[object]" in {
    eventually {
      id("object-future-obj-str-output").element.text should be ("string")
      id("object-future-obj-num-output").element.text should be ("44")
    }
  }

  "The future field that contains an object with its own embedded futures" should "set the two fields from the embedded array[future]" in {
    eventually {
      id("object-future-obj-arr1-output").element.text should be ("arr[0]")
      id("object-future-obj-arr2-output").element.text should be ("arr[1]")
    }
  }


}
