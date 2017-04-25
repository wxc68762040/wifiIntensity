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
	val verticalHeight = 1.45
	val referenceRSSI = -30
	val scaling = 1920.0 / 32.09
	val bufferLimit = 30
	def props(boxMac: String, rssiSet: Int, distanceLoss: Double) = Props(new BoxWorker(boxMac, rssiSet, distanceLoss))
}

class BoxWorker(boxMac: String, rssiSet: Int, distanceLoss: Double) extends Actor {
	
	import BoxWorker._
	private[this] val log = LoggerFactory.getLogger(this.getClass)
	private[this] val logPrefix = context.self.path
	private[this] val selfRef = context.self
	private[this] val shootBuffer = scala.collection.mutable.ListBuffer[rBasicShoot]()
	
	private[this] def getDistance(rssi1: Double, rssi2: Double, distanceLoss: Double) = {
		val realDistance1 = Math.pow(10, (referenceRSSI - rssi1) / (10 * distanceLoss))
		val realDistance2 = Math.pow(10, (referenceRSSI - rssi2) / (10 * distanceLoss))
		val realDistance = (realDistance1 + realDistance2) / 2
		val horizontalDistance = Math.sqrt(realDistance * realDistance - verticalHeight * verticalHeight)
		horizontalDistance * scaling
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
			val validShoots = shoots.filter(e => e.rssi(0) > rssiSet && e.rssi(1) > rssiSet)
				.groupBy(e => (e.clientMac, e.t))
				.map {e =>
					val size = e._2.size
					val rssi1 = e._2.map(e => e.rssi(0)).sum.toDouble / size
					val rssi2 = e._2.map(e => e.rssi(1)).sum.toDouble / size
					rBasicShoot(-1L, boxMac, e._1._1, e._1._2, rssi1, rssi2, getDistance(rssi1, rssi2, distanceLoss))
				}
			log.info(s"$boxMac get shoots, after filter, size: ${validShoots.size}")
			shootBuffer ++= validShoots
			if(shootBuffer.size >= bufferLimit) {
				context.parent ! SaveRequest(shootBuffer.toList)
				shootBuffer.clear()
			}
		
		case msg =>
			log.warn(s"$logPrefix get unknown message: $msg")
	}
}
