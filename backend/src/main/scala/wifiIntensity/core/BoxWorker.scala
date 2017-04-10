package wifiIntensity.core

import akka.actor.{Actor, Props}
import org.slf4j.LoggerFactory
import wifiIntensity.core.BoxManager.SaveRequest
import wifiIntensity.models.tables.rBasicShoot
import wifiIntensity.protocol.PutShoots

/**
	* Created by 流風幻葬 on 2017/3/31.
	*/

object BoxWorker {
	def props(boxMac: String, rssiSet: Int, distanceLoss: Double) = Props(new BoxWorker(boxMac, rssiSet, distanceLoss))
}

class BoxWorker(boxMac: String, rssiSet: Int, distanceLoss: Double) extends Actor {
	
	private[this] val log = LoggerFactory.getLogger(this.getClass)
	private[this] val logPrefix = context.self.path
	private[this] val selfRef = context.self
	private[this] val shootBuffer = scala.collection.mutable.ListBuffer[rBasicShoot]()
	
	private[this] def getDistanceRatio(rssi1: Int, rssi2: Int, distanceLoss: Double) = {
		Math.pow(10, (rssi2 - rssi1).toDouble / (10 * distanceLoss))
	}
	
	override def preStart = {
		log.info(s"$logPrefix is now starting.")
		log.info(s"$logPrefix's father is ${context.parent.path.name}")
	}
	
	override def postStop = {
		log.info(s"$logPrefix is stopped.")
		context.parent ! SaveRequest(shootBuffer.toList)
	}
	
	override def receive = working
	
	def working: Receive = {
		case PutShoots(_, shoots) =>
			val validShoots = shoots.filter(e => Math.abs(e.rssi(0)) > rssiSet && Math.abs(e.rssi(1)) > rssiSet).map {e =>
				rBasicShoot(-1L, boxMac, e.clientMac, e.t, Math.abs(e.rssi(0)), Math.abs(e.rssi(1)), getDistanceRatio(e.rssi(0), e.rssi(1), distanceLoss))
			}
			log.info(s"$boxMac get shoots, after filter, size: ${validShoots.size}")
			shootBuffer ++= validShoots
			if(shootBuffer.size >= 100) {
				context.parent ! SaveRequest(shootBuffer.toList)
				shootBuffer.clear()
			}
		
		case msg =>
			log.warn(s"$logPrefix get unknown message: $msg")
	}
}
