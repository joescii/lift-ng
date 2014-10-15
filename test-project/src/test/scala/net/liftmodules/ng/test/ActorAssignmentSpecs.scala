package net.liftmodules.ng.test

class ActorAssignmentSpecs extends BaseSpec {
  "The Actors - Assignment page" should "load" in {
    initialize("actorsAssignment")
  }

  "The comet button" should "cause all of the data to load" in {
    click on "button-comet"
    eventually {
      id("root-str").element.text should be ("Root String")
      id("root-obj-str").element.text should be ("a")
      id("root-obj-int").element.text should be ("1")
      id("scope-str").element.text should be ("Scope String")
      id("scope-obj-str").element.text should be ("b")
      id("scope-obj-int").element.text should be ("2")
    }
  }
}
