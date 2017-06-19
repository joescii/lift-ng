package net.liftmodules.ng.test

class FailureHandlerSpec extends BaseSpec {
  "The failure handler page" should "load" in {
    initialize("failure-handler")
  }

  "defAny when returning a failure" should "work" in {
    eventually { id("any-failure").element.text should be ("defAny_failure test") }
  }

  "defStringToAny when returning a failure" should "work" in {
    eventually { id("string-to-any-failure").element.text should be ("defStringToAny_failure test") }
  }

  "defModelToAny when returning a failure" should "work" in {
    eventually { id("model-to-any-failure").element.text should be ("defModelToAny_failure test") }
  }

  "defFutureAny when returning a failure" should "work" in {
    eventually { id("future-any-failure").element.text should be ("defFutureAny_failure test") }
  }

  "defStringToFutureAny when returning a failure" should "work" in {
    eventually { id("string-to-future-any-failure").element.text should be ("defStringToFutureAny_failure test") }
  }

  "defModelToFutureAny when returning a failure" should "work" in {
    eventually { id("model-to-future-any-failure").element.text should be ("defModelToFutureAny_failure test") }
  }

  "defAny when throwing an exception" should "work" in {
    eventually { id("any-exception").element.text should be ("defAny_exception test") }
  }

  "defStringToAny when throwing an exception" should "work" in {
    eventually { id("string-to-any-exception").element.text should be ("defStringToAny_exception test") }
  }

  "defModelToAny when throwing an exception" should "work" in {
    eventually { id("model-to-any-exception").element.text should be ("defModelToAny_exception test") }
  }

  "defFutureAny when throwing an exception outside of the Future" should "work" in {
    eventually { id("future-any-outside-exception").element.text should be ("defFutureAny_outer_exception test") }
  }

  "defStringToFutureAny when throwing an exception outside of the Future" should "work" in {
    eventually { id("string-to-future-any-outside-exception").element.text should be ("defStringToFutureAny_outer_exception test") }
  }

  "defModelToFutureAny when throwing an exception outside of the Future" should "work" in {
    eventually { id("model-to-future-any-outside-exception").element.text should be ("defModelToFutureAny_outer_exception test") }
  }
}
