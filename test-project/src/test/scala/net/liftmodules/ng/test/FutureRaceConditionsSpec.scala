package net.liftmodules.ng.test

class FutureRaceConditionsSpec extends BaseSpec {
  "The future race condition page" should "load" in {
    initialize("future-race-condition")
    click on "go-button"
  }

  "The numbers 1-10" should "resolve" in {
    (0 to 9).foreach { i =>
      eventually { id(s"f$i").element.text should be ((i+1).toString) }
    }
  }
}
