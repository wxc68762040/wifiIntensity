package wifiIntensity.core

import akka.actor.{Actor, ActorRef, Props, Stash, Terminated}
import org.slf4j.LoggerFactory
import wifiIntensity.core.BoxManager._
import wifiIntensity.models.dao.{BasicShootDAO, BoxDAO, ClientLocationDAO}
import wifiIntensity.models.tables.{rBasicShoot, rClientLocation}
import wifiIntensity.protocol.SubscribeData
import com.github.nscala_time.time.Imports.DateTime
import org.apache.commons.math3.stat.regression.SimpleRegression

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
	* Created by 流風幻葬 on 2017/3/30.
	*/
object BoxManager {
	
	def props(wsClient: ActorRef) = Props(new BoxManager(wsClient))
	
	case class InitDone(boxList: List[(String, Int, Double, Double, Double)])
	case class SaveRequest(shoots: List[rBasicShoot])
	case class RegularlyCounting(start:Long, end: Long)
	case object WorkDone
}

class BoxManager(wsClient: ActorRef) extends Actor with Stash{
	
	private[this] val log = LoggerFactory.getLogger(this.getClass)
	private[this] val logPrefix = context.self.path
	private[this] val selfRef = context.self
	private[this] val boxInfo = scala.collection.mutable.HashMap[String, (Double, Double)]()
	
	def getInitMillis = {
		val delayTargetMinute = 2
		DateTime.now.plusMinutes(delayTargetMinute).withSecondOfMinute(0).getMillis - DateTime.now.getMillis
	}
	
	def getBoxWorker(boxMac: String, rssiSet: Int, distanceLoss: Double): ActorRef = {
		context.child(boxMac).getOrElse {
			val child = context.actorOf(BoxWorker.props(boxMac, rssiSet, distanceLoss), boxMac)
			log.info(s"From BoxManager: $logPrefix $boxMac is starting.")
			context.watch(child)
			child
		}
	}
	
	context.system.scheduler.schedule(
		getInitMillis millis,
		1 minute,
		self,
		RegularlyCounting(
			DateTime.now.minusMinutes(2).withSecondOfMinute(0).withMillisOfSecond(0).getMillis,
			DateTime.now.minusMinutes(1).withSecondOfMinute(0).withMillisOfSecond(0).getMillis)
	)
	
	override def preStart = {
		log.info(s"$logPrefix is now starting.")
		context.setReceiveTimeout(2 minutes)
		BoxDAO.getAllBoxs.map { res =>
			selfRef ! InitDone(res.map(e => (e.boxMac, e.rssiSet, e.distanceLoss, e.x, e.y)).toList)
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
				boxInfo.put(e._1, (e._4, e._5))
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
			
		case RegularlyCounting(start: Long, end: Long) =>
			BasicShootDAO.getShootsByTime(start, end).map{ list =>
				val distanceInfo = list.groupBy(_.clientMac).map{ userRecords => //clientMac -> records
					val records = userRecords._2.groupBy(_.boxMac).map{ e => // clientMac -> boxMac -> records
						val recordSize = e._2.size
						(e._1, e._2.map(_.distance).sum / recordSize)
					}
					if(records.size >= 3) {
						val reg = new SimpleRegression(true)
						val x1 = boxInfo.getOrElse(records.head._1, (0.0, 0.0))._1
						val y1 = boxInfo.getOrElse(records.head._1, (0.0, 0.0))._2
						val d1 = records.head._2
						records.drop(1).foreach { e =>
							val x = boxInfo.getOrElse(e._1, (0.0, 0.0))._1
							val y = boxInfo.getOrElse(e._1, (0.0, 0.0))._2
							val d = e._2
							val u = (y1 - y) / (x1 - x)
							val v = (d * d - d1 * d1 + x1 * x1 + y1 * y1 - x * x - y * y) / (2 * (x1 - x))
							reg.addData(u, v)
						}
						(userRecords._1, (reg.getIntercept, reg.getSlope))
					}
					else {
						("dummy", (0.0, 0.0))
					}
				}.filter(_._1 != "dummy").map(e => rClientLocation(-1, e._1, start, e._2._1, e._2._2)).toList
				ClientLocationDAO.addRecords(distanceInfo).map {
					case Success(num) =>
						log.info(s"insert client location success, size: $num")
					case Failure(e) =>
						log.error(s"insert client location FAILED: $e")
				}
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
