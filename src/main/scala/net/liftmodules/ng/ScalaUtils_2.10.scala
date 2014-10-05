package net.liftmodules.ng

/** Some utils which aren't available prior to Scala 2.10 */
trait ScalaUtils {
  def empty_pf[A, B] : PartialFunction[A, B] = PartialFunction.empty
}
