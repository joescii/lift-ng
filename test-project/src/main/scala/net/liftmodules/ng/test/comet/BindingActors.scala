package net.liftmodules.ng
package test.comet

class CounterBindActor extends BindingActor {
  override val bindTo = "currentCount"
}

class ArrayBindActor extends BindingActor {
  override val bindTo = "array"
}

class C2sBindActor extends BindingActor {
  override val bindTo = "inputTxt"
}

class C2sReturnActor extends AngularActor {

}