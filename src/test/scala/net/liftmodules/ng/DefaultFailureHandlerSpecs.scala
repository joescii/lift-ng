package net.liftmodules.ng

import net.liftmodules.ng.Angular._
import net.liftweb.common.Failure
import net.liftweb.json.JsonAST.JString
import org.scalatest.{Matchers, WordSpec}

class DefaultFailureHandlerSpecs extends WordSpec with Matchers {
  val dirtyException = new Exception(
    """the future failed
      |quite badly""".stripMargin
  )
  val cleanString = "the future failed\\u000aquite badly"

  "The DefaultApiSuccessMapper.tryToPromise" should {
    "clean up newlines in exception messages" in {
      val f: Failure = throwableToFailure(dirtyException)
      val p: Promise = Angular.defaultFailureHandler(f)

      p shouldEqual Reject(JString(cleanString))
    }
  }
}
