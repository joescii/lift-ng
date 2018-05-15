package bootstrap.liftweb

import net.liftweb.common.Loggable
import net.liftweb.util.Props
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

import scala.util.{Failure, Properties, Try}

object Start extends Loggable {
  def initCluster(): Unit = {}

  def main(args: Array[String]): Unit = {
    startLift()
  }

  def startLift(): Unit = {
    logger.info("Starting Lift server...")

    val webappDir: String = Option(this.getClass.getClassLoader.getResource("webapp"))
      .map(_.toExternalForm)
      .filter(_.contains("jar:file:")) // this is a hack to distinguish in-jar mode from "expanded"
      .getOrElse("target/webapp")

    logger.debug(s"webappDir: $webappDir")

    val port = System.getProperty(
      "jetty.port", Properties.envOrElse("PORT", "8080")).toInt
    val server = new Server(port)
    val context = new WebAppContext(webappDir, Props.get("jetty.contextPath").openOr("/"))

    server.setHandler(context)

    val attempts = Stream.from(1).takeWhile(_ <= 60)
      .map { attemptNumber =>
        val attempt = Try(server.start())
        attempt.failed.foreach { ex =>
          logger.info(s"Attempt number $attemptNumber of 60 to start jetty failed.")
          logger.debug("The exception", ex)
          Thread.sleep(1000)
        }
        attempt
      }

    val firstSuccess = attempts.find(_.isSuccess)

    firstSuccess match {
      case Some(_) =>
        logger.info(s"Lift server started on port $port")
        server.join()
      case _ =>
        logger.error(s"Exhausted 60 attempts to start Jetty!")
        attempts.zipWithIndex.collect {
          case (Failure(ex), i) => logger.error(s"Exception from attempt $i", ex)
        }
    }
  }
}
