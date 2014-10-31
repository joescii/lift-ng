package net.liftmodules.ng

import net.liftweb.http.LiftRules

trait AngularProperties {
  // This has to be optional because LiftRules.context is null during unit testing
  val ajaxEndpoint = Option(LiftRules.context) map ("'"+_.path+"/ajax_request/'+lift_page+'/'")
}
