package net.liftmodules.ng.test

class EmbeddedFuturesSpec extends BaseSpec {
  "The futures page" should "load" in {
    initialize("embedded-futures")
    click on "go-button"
  }

  "The future field that resolves before sending" should "be set to 'resolved'" ignore {
    eventually { id("resolved-output").element.text should be ("resolved") }
  }

  "The future field that fails" should "be set to 'failed'" ignore {
    eventually { id("failed-output").element.text should be ("failed") }
  }

  "The future field that contains a string" should "be set to 'future'" ignore {
    eventually { id("string-output").element.text should be ("future") }
  }

  "The future field that contains an object" should "set its two fields" ignore {
    eventually {
      id("object-str-output").element.text should be ("string")
      id("object-num-output").element.text should be ("42")
    }
  }

  "The future field that contains an object with its own embedded futures" should "set its num and str fields" ignore {
    eventually {
      id("object-future-str-output").element.text should be ("string")
      id("object-future-num-output").element.text should be ("43")
      id("object-future-fail-output").element.text should be ("failed2")
    }
  }

  "The future field that contains an object with its own embedded futures" should "set the two fields from the embedded future[object]" ignore {
    eventually {
      id("object-future-obj-str-output").element.text should be ("string")
      id("object-future-obj-num-output").element.text should be ("44")
    }
  }


}
