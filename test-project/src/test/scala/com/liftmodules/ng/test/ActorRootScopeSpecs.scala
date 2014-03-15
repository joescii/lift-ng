package com.liftmodules.ng.test

/** Specs for the angular services added via snippets */
class ActorRootScopeSpecs extends BaseSpec {
  "The Actor - Root Scope page" should "load" in {
    go to s"$index/actorsRootScope"
    eventually { pageTitle should be ("App: Actors - Root Scope") }
  }

  def cometStringCheck(button:String, span:String) = {
    click on button
    eventually { id(span).element.text should be ("0") }
    eventually { id(span).element.text should be ("1") }
    eventually { id(span).element.text should be ("2") }
    eventually { id(span).element.text should be ("3") }
    eventually { id(span).element.text should be ("4") }
  }

  "The angular RootScopeBroadcastString comet actor" should "update the async block continually with consecutive integers" in {
    cometStringCheck("button3", "rootScopeBroadcastStringOut")
  }

  def cometJsonCheck(button:String, spanInt:String, spanChar:String) = {
    click on button
    eventually {
      id(spanInt).element.text should be ("0")
      id(spanChar).element.text should be ("a")
    }
    eventually {
      id(spanInt).element.text should be ("1")
      id(spanChar).element.text should be ("b")
    }
    eventually {
      id(spanInt).element.text should be ("2")
      id(spanChar).element.text should be ("c")
    }
    eventually {
      id(spanInt).element.text should be ("3")
      id(spanChar).element.text should be ("d")
    }
    eventually {
      id(spanInt).element.text should be ("4")
      id(spanChar).element.text should be ("e")
    }
  }

  "The angular RootScopeBroadcastJson comet actor" should "update the async block continually with consecutive integers and letters" in {
    cometJsonCheck("button4", "rootScopeBroadcastJsonOut1", "rootScopeBroadcastJsonOut2")
  }

  "The angular RootScopeEmitString comet actor" should "update the async block continually with consecutive integers" in {
    cometStringCheck("button5", "rootScopeEmitStringOut")
  }

  "The angular RootScopeEmitJson comet actor" should "update the async block continually with consecutive integers and letters" in {
    cometJsonCheck("button6", "rootScopeEmitJsonOut1", "rootScopeEmitJsonOut2")
  }
}
