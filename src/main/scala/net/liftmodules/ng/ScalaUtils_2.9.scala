package net.liftmodules.ng

/** Some utils which aren't available prior to Scala 2.10 */
trait ScalaUtils {
  def empty_pf[A, B] : PartialFunction[A, B] = new PartialFunction[Any, Nothing] {
    def isDefinedAt(x: Any) = false
    def apply(x: Any) = throw new MatchError(x)
    override def orElse[A1, B1](that: PartialFunction[A1, B1]) = that
    override def andThen[C](k: Nothing => C) = this
    override val lift = (x: Any) => None
  }
}