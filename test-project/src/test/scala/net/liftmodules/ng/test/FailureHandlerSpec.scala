package net.liftmodules.ng.test

class FailureHandlerSpec extends BaseSpec {
  "The failure handler page" should "load" in {
    initialize("failure-handler")
  }

  "The defAny returning a failure" should "work" in {
    eventually { id("any-failure").element.text should be ("defAny_failure test") }
  }

  "The defStringToAny returning a failure" should "work" in {
    eventually { id("string-to-any-failure").element.text should be ("defStringToAny_failure test") }
  }

  "The defModelToAny returning a failure" should "work" in {
    eventually { id("model-to-any-failure").element.text should be ("defModelToAny_failure test") }
  }

  "The defFutureAny returning a failure" should "work" in {
    eventually { id("future-any-failure").element.text should be ("defFutureAny_failure test") }
  }

  "The defStringToFutureAny returning a failure" should "work" in {
    eventually { id("string-to-future-any-failure").element.text should be ("defStringToFutureAny_failure test") }
  }

  "The defModelToFutureAny returning a failure" should "work" in {
    eventually { id("model-to-future-any-failure").element.text should be ("defModelToFutureAny_failure test") }
  }

}
