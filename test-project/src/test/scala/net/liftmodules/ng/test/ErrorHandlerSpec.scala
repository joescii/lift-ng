package net.liftmodules.ng.test

class ErrorHandlerSpec extends BaseSpec {
  "The error handler page" should "load" in {
    initialize("error-handler")
  }

  "The defFutureAny returning a failure" should "work" in {
    eventually { id("future-any-failure").element.text should be ("defFutureAny_failure test") }
  }

}
