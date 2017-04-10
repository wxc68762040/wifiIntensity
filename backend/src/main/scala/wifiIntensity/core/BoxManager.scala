package wifiIntensity.core

import akka.actor.{Actor, ActorRef, Props, Stash, Terminated}
import org.slf4j.LoggerFactory
import wifiIntensity.core.BoxManager._
import wifiIntensity.models.dao.{BasicShootDAO, BoxDAO}
import wifiIntensity.models.tables.rBasicShoot
import wifiIntensity.protocol.SubscribeData

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
	* Created by 流風幻葬 on 2017/3/30.
	*/
object BoxManager {
	
	def props(wsClient: ActorRef) = Props(new BoxManager(wsClient))
	
	case class InitDone(boxList: List[(String, Int, Double)])
	case class SaveRequest(shoots: List[rBasicShoot])
	case object WorkDone
}

class BoxManager(wsClient: ActorRef) extends Actor with Stash{
	
	private[this] val log = LoggerFactory.getLogger(this.getClass)
	private[this] val logPrefix = context.self.path
	private[this] val selfRef = context.self
	
	def getBoxWorker(boxMac: String, rssiSet: Int, distanceLoss: Double): ActorRef = {
		context.child(boxMac).getOrElse {
			val child = context.actorOf(BoxWorker.props(boxMac, rssiSet, distanceLoss), boxMac)
			log.info(s"From BoxManager: $logPrefix $boxMac is starting.")
			context.watch(child)
			child
		}
	}
	
	override def preStart = {
		log.info(s"$logPrefix is now starting.")
		context.setReceiveTimeout(2 minutes)
		BoxDAO.getAllBoxs.map { res =>
			selfRef ! InitDone(res.map(e => (e.boxMac, e.rssiSet, e.distanceLoss)).toList)
		}
	}
	
	override def postStop = {
		log.info(s"$logPrefix is stopped.")
	}
	
	override def receive = init
	
	def init: Receive = {
		case InitDone(boxList) =>
			context.setReceiveTimeout(Duration.Undefined)
			log.info(s"get init done signal, boxList size: ${boxList.size}")
			boxList.foreach { e =>
				val actor = getBoxWorker(e._1, e._2, e._3)
				wsClient ! SubscribeData(actor, e._1)
			}
			unstashAll()
			context.become(working)
			
		case _ =>
			stash()
	}
	
	def working: Receive = {
		
		case SaveRequest(shoots) =>
			context.become(busy)
			BasicShootDAO.addShoots(shoots).map {
				case Success(_) =>
					log.info(s"Insert shoots success, size: ${shoots.size}")
					selfRef ! WorkDone
				case Failure(e) =>
					log.error(s"Insert shoots FAILED: ${e.getMessage}")
					selfRef ! WorkDone
			}
			
		case Terminated(child) =>
			log.error(s"$logPrefix child terminated: ${child.path.name} is dead.")
		
		case msg =>
			log.warn(s"$logPrefix get unknown message: $msg")
	}
	
	def busy: Receive = {
		case WorkDone =>
			unstashAll()
			context.become(working)
			
		case _ =>
			stash()
	}
	
}
