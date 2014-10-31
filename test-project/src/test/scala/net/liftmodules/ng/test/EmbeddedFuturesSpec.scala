package net.liftmodules.ng.test

class EmbeddedFuturesSpec extends BaseSpec {
  "The futures page" should "load" in {
    initialize("embedded-futures")
  }

  "The object" should "load all its stuff" ignore {
    click on "go-button"
    eventually {
      id("resolved-output").element.text should be ("resolved")
      id("failed-output").element.text should be ("failed")
      id("string-output").element.text should be ("future")
      id("object-str-output").element.text should be ("string")
      id("object-num-output").element.text should be ("42")
    }
  }

}
