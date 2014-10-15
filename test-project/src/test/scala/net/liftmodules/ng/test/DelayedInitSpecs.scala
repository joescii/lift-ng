package net.liftmodules.ng.test

import org.scalatest.time.{Millis, Seconds, Span}

class DelayedInitSpecs extends BaseSpec {
  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(3, Seconds)), interval = scaled(Span(5, Millis)))

  "The Delay page" should "load" in {
    // This page is a special case where we shouldn't rely on the initialize function
    go to s"$index/delay"
    eventually { pageTitle should be ("App: Delay") }
  }

  "The integers 1 - 3" should "load" in {
    eventually { id("last-msg").element.text should be ("3") }
    val divs = findAll(className("early-emit-out"))
    val ints = 0 to 3 map (_.toString)
    divs.drop(1).map(_.text).toList should equal (ints)
  }

  "The integers 4 - 6" should "load after pressing 'Go'" in {
    click on "button-go"
    eventually { id("last-msg").element.text should be ("4") }
    eventually { id("last-msg").element.text should be ("5") }
    eventually { id("last-msg").element.text should be ("6") }
    val divs = findAll(className("early-emit-out"))
    val ints = 0 to 6 map (_.toString)
    divs.drop(1).map(_.text).toList should equal (ints)
  }
}
