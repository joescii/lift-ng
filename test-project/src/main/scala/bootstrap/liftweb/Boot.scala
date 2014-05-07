package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import net.liftmodules.JQueryModule
import net.liftweb.http.js.jquery._
import net.liftmodules.ng.Angular
import java.util.ResourceBundle
import java.util
import net.liftweb.util


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("net.liftmodules.ng.test")

    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index", // the simple way to declare a menu
      Menu.i("Snippets") / "snippets",
      Menu.i("Futures") / "futures",
      Menu.i("Two Apps") / "twoApps",
      Menu.i("Static") / "static",
      Menu.i("i18n - 1 name") / "i18n-1name",
      Menu.i("i18n - Non-English") / "i18n-non-english",
      Menu.i("i18n - 2 names") / "i18n-2names",
      Menu.i("Actors - Root Scope") / "actorsRootScope",
      Menu.i("Actors - Scope") / "actorsScope",
      Menu.i("Actors - Assignment") / "actorsAssignment",
      Menu.i("Delay") / "delay"
    )

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery172
    JQueryModule.init()

    LiftRules.resourceNames =
      "testBundle" ::
      "nonEnglish" ::
      LiftRules.resourceNames

    angular()
  }

  def angular() = {
    import net.liftmodules.ng._
    
    Angular.init(UnmanagedLibs(futures = false, appSelector = ".application"))
  }
}
