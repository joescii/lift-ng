package net.liftmodules.ng.test

class I18nSpecs extends BaseSpec{
  "The i18n-1name page" should "load" in {
    initialize("i18n-1name")
  }

  "The no-arg value on the i18n-1name page" should "be 'Howdy!'" in {
    id("no-params").element.text should be ("Howdy!")
  }

  "The 1-arg value on the i18n-1name page" should "be 'Goodbye, Cruel World!'" in {
    id("params").element.text should be ("Goodbye, Cruel World!")
  }


  "The i18n-non-english page" should "load" in {
    initialize("i18n-non-english")
  }

  "The no-arg value on the i18n-non-english page" should "be the correct Chinese" in {
    id("no-params").element.text should be ("\u4F60\u597D")
  }

  "The 1-arg value on the i18n-non-english page" should "be the correct Hebrew" in {
    id("params").element.text should be ("!Cruel World ,\u05E9\u05DC\u05D5\u05DD")
  }


  "The i18n-2names page" should "load" in {
    initialize("i18n-2names")
  }

  "The no-arg value on the i18n-2names page" should "be 'Howdy!'" in {
    id("no-params").element.text should be ("Howdy!")
  }

  "The 1-arg value on the i18n-2names page" should "be 'Goodbye, Cruel World!'" in {
    id("params").element.text should be ("Goodbye, Cruel World!")
  }

  "The lift-bundle value on the i18n-2names page" should "be 'Lost Password'" in {
    id("other").element.text should be ("Lost Password")
  }


}
