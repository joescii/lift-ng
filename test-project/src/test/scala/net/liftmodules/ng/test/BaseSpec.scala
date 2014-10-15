package net.liftmodules.ng.test

import org.scalatest._
import concurrent.Eventually
import time._
import selenium._

import org.openqa.selenium._
import firefox.FirefoxDriver
//import safari.SafariDriver
//import chrome.ChromeDriver
//import ie.InternetExplorerDriver

trait BaseSpec extends FlatSpecLike with ShouldMatchers with Eventually with WebBrowser with BeforeAndAfterAll {
  override def afterAll = close()

  val index    = "http://localhost:8080"

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(100, Millis)))

  implicit val webDriver: WebDriver = Option(System.getProperty("net.liftmodules.ng.test.browser")) match {
    case Some("firefox") => new FirefoxDriver() // Currently only this one will work due to need for drivers of the others.
    //    case Some("chrome") => new ChromeDriver()
    //    case Some("ie32") => new InternetExplorerDriver()
    //    case Some("ie64") => new InternetExplorerDriver()
    //    case Some("safari") => new SafariDriver()
    case _ => new FirefoxDriver()
  }

  def initialize(page:String) = {
    go to s"$index/$page"
    eventually { id("ready").element.text should be ("Ready") }
  }

}
