package net.liftmodules.ng.test

class SubdirI18nSpecs extends BaseSpec{
  "The i18n-1name page" should "load" in {
    initialize("subdir/i18n-1name")
  }

  "The no-arg value on the i18n-1name page" should "be 'Howdy!'" in {
    id("no-params").element.text should be ("Howdy!")
  }

  "The 1-arg value on the i18n-1name page" should "be 'Goodbye, Cruel World!'" in {
    id("params").element.text should be ("Goodbye, Cruel World!")
  }

}
