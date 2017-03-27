package wifiIntensity.core

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, WebSocketRequest}
import akka.stream._
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteStringBuilder
import org.slf4j.LoggerFactory
import wifiIntensity.protocol.{PutShoots, SubscribeData}
import wifiIntensity.utils.{SecureUtil, ShootUtil}
import wifiIntensity.common.AppSettings._

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Created by liuziwei on 2016/7/4.
  */
object WsClient {

  private[this] val log = LoggerFactory.getLogger(this.getClass)

  val strategy: Supervision.Decider = {
    case e: Exception =>
      log.error("$materializer error", e)
      Supervision.Stop
  }

  def props(_system: ActorSystem, _materializer: Materializer, _executor: ExecutionContextExecutor) = Props[WsClient](
    new WsClient {
      override implicit val system: ActorSystem = _system
      override implicit val materializer: Materializer = _materializer
      override implicit val executor: ExecutionContextExecutor = _executor
    }
  )

  case object Connect2Akso

}

trait WsClient extends Actor {

  import WsClient._

  import scala.concurrent.duration._

  private[this] val log = LoggerFactory.getLogger(this.getClass)
  private[this] val logPrefix = context.self.path

  private val dataBus = new DataBus()

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    log.info(s"$logPrefix starting...")
    self ! Connect2Akso
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    log.info(s"$logPrefix stopping...")
  }

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  def abs(x: Long) = if (x > 0) x else -x


  val incoming =
    Sink.foreach[Message] {
      case msg: BinaryMessage.Streamed =>
        val f = msg.dataStream.runFold(new ByteStringBuilder().result()) {
          case (s, str) => s.++(str)
        }
        f.map { s =>
          s.decodeString("UTF-8").split("\u0001").toList.flatMap { i =>
            ShootUtil.line2Shoot(i)
          }.groupBy(_.boxMac).foreach { case (boxMac, shoots) =>
            dataBus.publish((DataBus.getClassify(boxMac), PutShoots(boxMac, shoots)))
          }
        }
      case unknown =>
        log.error(s"$logPrefix receive unknown message:$unknown")
    }

  override def receive: Receive = {
    case Connect2Akso =>

      val ((timestamp, nonce, sign)) = SecureUtil.generateSignatureParameters(List(aksoAppId), aksoSecureKey)
      val url = s"ws://$aksoHost:$aksoPort/akso/subscribe/data?nonce=$nonce&timestamp=$timestamp&appId=$aksoAppId&signature=$sign"

      val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))
      val ((stream, response), closed) =
        Source.actorRef(1, OverflowStrategy.fail)
          .viaMat(webSocketFlow)(Keep.both) // keep the materialized Future[WebSocketUpgradeResponse]
          .toMat(incoming)(Keep.both) // also keep the Future[Done]
          .run()
      //  val closed = webSocketFlow.watchTermination().andThen()
      val connected = response.flatMap { upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          Future.successful(s"$logPrefix connect success.")
        } else {
          throw new RuntimeException(s"$logPrefix connection failed: ${upgrade.response.status}")
        }
      } //链接建立时
      connected.onComplete(i => log.info(i.toString))
      closed.onComplete { i =>
        log.error(s"$logPrefix connect to akso closed! try again 1 minutes later")
        context.system.scheduler.scheduleOnce(1.minute, self, Connect2Akso)
      } //链接断开时

    case SubscribeData(peer, boxMac) =>
      log.info(s"$logPrefix $boxMac register data...")
      context.watch(peer)
      dataBus.subscribe(peer, DataBus.getClassify(boxMac))

  }
}
