package net.liftmodules.ng

trait AngularProperties {
  val ajaxEndpoint = Some("window.lift_settings.liftPath+'/ajax/'+window.lift.getPageId()+'/'")
  val ajaxFn = "lift.ajax"
}
