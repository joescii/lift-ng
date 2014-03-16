package com.liftmodules.ng.test

class ActorScopeSpecs extends BaseSpec {
  "The Actors - Scope page" should "load" in {
    go to s"$index/actorsScope"
    eventually { pageTitle should be ("App: Actors - Scope") }
  }

  "The emit button" should "cause the parent and actor scopes to load, but not the child scope" in {
    click on "button-emit"
    eventually {
      id("parent-msg").element.text should be ("emit")
      id("actor-msg").element.text should be ("emit")
    }
    id("child-msg").element.text should not be ("emit")
  }

  "The broadcast button" should "cause the actor and child scopes to load, but not the parent scope" in {
    click on "button-broadcast"
    eventually {
      id("actor-msg").element.text should be ("broadcast")
      id("child-msg").element.text should be ("broadcast")
    }
    id("parent-msg").element.text should not be ("broadcast")
  }
}
