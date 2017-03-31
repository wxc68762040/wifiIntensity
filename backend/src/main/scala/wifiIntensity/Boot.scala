package wifiIntensity

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.slf4j.LoggerFactory
import wifiIntensity.core.{BoxManager, WsClient}
import wifiIntensity.http.HttpService
import wifiIntensity.utils.AppClientKeys
import wifiIntensity.common.AppSettings._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by liuziwei on 2016/7/4.
  * Updated by zhangtao on 2016/12/15
  */
object Boot extends HttpService {

  override val log = LoggerFactory.getLogger(this.getClass)

  override implicit val system = ActorSystem("wifiIntensity")
  override implicit val executor = system.dispatchers.lookup("akka.actor.my-blocking-dispatcher")
  override implicit val materializer = ActorMaterializer()

  override val timeout = Timeout(1 minutes) // for actor asks

  override val appClientKeys: ActorRef = system.actorOf(AppClientKeys.props, "AppClientKeys")
  val wsClient = system.actorOf(WsClient.props(system,materializer,executor),"WsClient")
  override val boxManager = system.actorOf(BoxManager.props(wsClient), "BoxManager")


  def main(args: Array[String]) {
    log.info("Starting.")
    val binding = Http().bindAndHandle(routes, httpInterface, httpPort)
    binding.onComplete {
      case Success(b) ⇒
        val localAddress = b.localAddress
        log.info(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
      case Failure(e) ⇒
        log.error(s"Binding failed with ${e.getMessage}")
        system.terminate()
        System.exit(-1)
    }
  }

}
