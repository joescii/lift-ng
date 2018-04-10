package net.liftmodules.ng

import scala.concurrent.ExecutionContext

object AngularExecutionContext {
  private var _ec: ExecutionContext = ExecutionContext.global

  def apply(ec: ExecutionContext) {
    this._ec = ec
  }

  implicit def ec: ExecutionContext = _ec

  implicit val provider: ExecutionContextProvider = new ExecutionContextProvider {
    override def ec: ExecutionContext = _ec
  }
}

trait ExecutionContextProvider {
  def ec: ExecutionContext
}

object ExecutionContextProvider {
  implicit class EnhancedExecutionContext(e: ExecutionContext) {
    def asProvider: ExecutionContextProvider = new ExecutionContextProvider {
      override def ec: ExecutionContext = e
    }
  }

  val globalSerializable: ExecutionContextProvider = new ExecutionContextProvider with Serializable {
    override def ec: ExecutionContext = ExecutionContext.global
  }
}

