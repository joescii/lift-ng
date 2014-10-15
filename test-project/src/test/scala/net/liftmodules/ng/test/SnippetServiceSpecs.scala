package net.liftmodules.ng.test

/** Specs for the angular services added via snippets */
class SnippetServiceSpecs extends BaseSpec {
  "The snippets page" should "load" in {
    initialize("snippets")
  }

  "The angular service with no argument" should "populate the test text with 'FromServer'" in {
    click on "button1a"
    eventually { id("output1").element.text should be ("FromServer") }
  }

  "The angular service with one string argument" should "send the text box string to the server and then populate " +
    "the test text with 'FromServer FromClient'" in {
    textField("input1").value = "FromClient"
    click on "button1b"
    eventually { id("output1").element.text should be ("FromServer FromClient") }
  }

  "The angular service which fails" should "send 'FromServerFail' to the client as a Failure" in {
    click on "button1Fail"
    eventually { id ("output1Fail").element.text should be ("FromServerFail")}
  }

  "The angular service with a JSON argument" should "send both text box strings to the server and populate the test " +
    "text boxes with 'FromServer client1' and 'FromServer client2'" in {
    textField("input2a").value = "client1"
    textField("input2b").value = "client2"
    click on "button2"
    eventually {
      id("output2a").element.text should be ("FromServer client1")
      id("output2b").element.text should be ("FromServer client2")
    }
  }
}
