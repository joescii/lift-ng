package net.liftmodules.ng.test

class FailureHandlerSpec extends BaseSpec {
  "The failure handler page" should "load" in {
    initialize("failure-handler")
  }

  "The defFutureAny returning a failure" should "work" in {
    eventually { id("future-any-failure").element.text should be ("defFutureAny_failure test") }
  }

}
