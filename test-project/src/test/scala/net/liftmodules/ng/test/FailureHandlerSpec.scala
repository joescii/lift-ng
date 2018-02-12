package net.liftmodules.ng.test

class FailureHandlerSpec extends BaseSpec {
  "The failure handler page" should "load" in {
    initialize("failure-handler")
  }

  "defAny when returning a failure" should "work" in {
    eventually { id("any-failure").element.text should be ("defAny_failure test") }
  }

  "defParamToAny when returning a failure" should "work" in {
    eventually { id("param-to-any-failure").element.text should be ("defParamToAny_failure test") }
  }

  "defFutureAny when returning a failure" should "work" in {
    eventually { id("future-any-failure").element.text should be ("defFutureAny_failure test") }
  }

  "defParamToFutureAny when returning a failure" should "work" in {
    eventually { id("param-to-future-any-failure").element.text should be ("defParamToFutureAny_failure test") }
  }

  "defAny when throwing an exception" should "work" in {
    eventually { id("any-exception").element.text should be ("defAny_exception test") }
  }

  "defParamToAny when throwing an exception" should "work" in {
    eventually { id("param-to-any-exception").element.text should be ("defParamToAny_exception test") }
  }

  "defFutureAny when throwing an exception outside of the Future" should "work" in {
    eventually { id("future-any-outside-exception").element.text should be ("defFutureAny_outer_exception test") }
  }

  "defParamToFutureAny when throwing an exception outside of the Future" should "work" in {
    eventually { id("param-to-future-any-outside-exception").element.text should be ("defParamToFutureAny_outer_exception test") }
  }
}
