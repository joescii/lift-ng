package net.liftmodules.ng
package test.comet

class CounterBindActor extends BindingActor {
  override val bindTo = "count"
}

class ArrayBindActor extends BindingActor {
  override val bindTo = "array"
}

class C2sBindActor extends BindingActor {
  override val bindTo = "input"
}

class C2sReturnActor extends AngularActor {

}