package net.liftmodules.ng

import net.liftweb.http.LiftRules

trait AngularProperties {
  val ajaxEndpoint = "'"+LiftRules.context.path+"/ajax_request/'+lift_page+'/'"
}
