package bootstrap.liftweb

import net.liftmodules.cluster.jetty9._
import net.liftweb.common.Loggable
import net.liftweb.util.{Props, StringHelpers}

import scala.util.Properties

object Start extends Loggable {
  def main(args: Array[String]): Unit = {
    startLift()
    println("BYE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
  }

  def startLift(): Unit = {
    val port = System.getProperty(
      "jetty.port", Properties.envOrElse("PORT", "8080")).toInt

    logger.info(s"port number is $port")

    val context = Props.get("jetty.contextPath").openOr("/")

    val workerName = StringHelpers.randomString(10)

    logger.info(s"WorkerName: $workerName")

    val endpoint = new SqlEndpointConfig {
      override def endpoint: String = "jdbc:h2:mem:clusterdb;DB_CLOSE_DELAY=-1"
    }

    val config = Jetty9Config(
      port = port,
      host = None,
      contextPath = context,
      clusterConfig = Some(Jetty9ClusterConfig(workerName, DriverOther("org.h2.Driver"), endpoint)),
      webappPath = "target/webapp",
      returnAfterStart = true
    )
    Jetty9Starter.start(config)
  }
}
