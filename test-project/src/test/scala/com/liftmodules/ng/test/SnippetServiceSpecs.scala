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

  "The angular service with no argument" should "populate the test text with 'FromServer'" in {
    click on "button1a"
    eventually { id("output1").element.text should be ("FromServer") }
  }

  "The angular service with one string argument" should "send the text box string to the server and then populate " +
    "the test text with 'FromServer FromClient'" in {
    textField("input1").value = "FromClient"
    click on "button1b"
    eventually { id("output1").element.text should be ("FromServer FromClient") }
  }

  "The angular service with a JSON argument" should "send both text box strings to the server and populate the test" +
    "text boxes with 'FromServer client1' and 'FromServer client2'" in {
    textField("input2a").value = "client1"
    textField("input2b").value = "client2"
    click on "button2"
    eventually {
      id("output2a").element.text should be ("FromServer client1")
      id("output2b").element.text should be ("FromServer client2")
    }
  }
}
