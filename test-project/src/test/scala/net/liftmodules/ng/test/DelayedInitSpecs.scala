package net.liftmodules.ng.test

import org.scalatest.time.{Millis, Seconds, Span}

class DelayedInitSpecs extends BaseSpec {
  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(3, Seconds)), interval = scaled(Span(5, Millis)))

  "The Delay page" should "load" in {
    go to s"$index/delay"
    eventually { pageTitle should be ("App: Delay") }
  }

  "The integers" should "load starting at 6" in {
    eventually { id("last-msg").element.text should be ("6") }
    eventually { id("last-msg").element.text should be ("7") }
    eventually { id("last-msg").element.text should be ("8") }
    eventually { id("last-msg").element.text should be ("9") }
    eventually { id("last-msg").element.text should be ("10") }
  }

  "The final list" should "contain all integers in order" in {
    val divs = findAll(className("early-emit-out"))
    divs.drop(1).zipWithIndex.foreach { case (div, i) =>
      div.text should be (i.toString)
    }
  }
}
