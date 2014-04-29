package net.liftmodules.ng.test

class I18nSpecs extends BaseSpec{
  "The i18n-1name page" should "load" in {
    go to s"$index/i18n-1name"
    eventually { pageTitle should be ("App: i18n - 1 name") }
  }

  "The no-arg value" should "be 'Howdy!'" in {
    id("no-params").element.text should be ("Howdy!")
  }

  "The 1-arg value" should "be 'Goodbye, Cruel World!'" in {
    id("params").element.text should be ("Goodbye, Cruel World!")
  }


  "The i18n-non-english page" should "load" in {
    go to s"$index/i18n-non-english"
    eventually { pageTitle should be ("App: i18n - Non-English") }
  }

  "The no-arg value" should "be the correct Chinese" in {
    id("no-params").element.text should be ("\u4F60\u597D")
  }

  "The 1-arg value" should "be the correct Hebrew" in {
    id("params").element.text should be ("!Cruel World ,\u05E9\u05DC\u05D5\u05DD")
  }
}
