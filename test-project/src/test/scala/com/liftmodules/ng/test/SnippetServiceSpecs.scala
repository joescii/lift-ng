package com.liftmodules.ng.test

import org.scalatest._
import concurrent.Eventually
import time._
import selenium._

import org.openqa.selenium._
import firefox.FirefoxDriver
//import safari.SafariDriver
//import chrome.ChromeDriver
//import ie.InternetExplorerDriver

/** Specs for the angular services added via snippets */
class SnippetServiceSpecs extends FlatSpecLike with ShouldMatchers with Eventually with WebBrowser with BeforeAndAfterAll {
  override def afterAll = close()

  val index    = "http://localhost:8080"
  val snippets = s"$index/snippets"

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(100, Millis)))

  implicit val webDriver: WebDriver = Option(System.getProperty("net.liftmodules.ng.test.browser")) match {
    case Some("firefox") => new FirefoxDriver() // Currently only this one will work due to need for drivers of the others.
//    case Some("chrome") => new ChromeDriver()
//    case Some("ie32") => new InternetExplorerDriver()
//    case Some("ie64") => new InternetExplorerDriver()
//    case Some("safari") => new SafariDriver()
    case _ => new FirefoxDriver()
  }

  "Selenium" should "navigate to the home page" in {
    go to index
    eventually { pageTitle should be ("App: Home") }
  }

  "The snippets page" should "load" in {
    go to snippets
    eventually { pageTitle should be ("App: Snippets") }
  }

  "The angular service with no return value and the service with a return string" should "populate the test text" in {
    textField("input1a").value = "First"
    click on "button1a"
    textField("input1b").value = "Second"
    click on "button1b"
    eventually { id("output1").element.text should be ("First Second") }
  }
}
