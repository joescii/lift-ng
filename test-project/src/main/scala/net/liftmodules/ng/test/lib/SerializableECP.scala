package net.liftmodules.ng.test.lib

import scala.concurrent.ExecutionContext
import net.liftmodules.ng.ExecutionContextProvider

object SerializableECP { self =>
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val ecp: ExecutionContextProvider = new ExecutionContextProvider {
    override def ec: ExecutionContext = self.ec
  }
}
