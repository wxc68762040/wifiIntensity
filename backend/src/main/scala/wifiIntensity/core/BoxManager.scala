package wifiIntensity.core

import akka.actor.{Actor, ActorRef, Props, Stash, Terminated}
import akka.pattern.ask
import org.slf4j.LoggerFactory
import wifiIntensity.core.BoxManager._
import wifiIntensity.models.dao.{BasicShootDAO, BoxDAO, ClientLocationDAO}
import wifiIntensity.models.tables.{rBasicShoot, rClientLocation}
import wifiIntensity.protocol.{GetDistance, SubscribeData}
import com.github.nscala_time.time.Imports.DateTime
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.joda.time.format.DateTimeFormat
import wifiIntensity.utils.FileUtil

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
	* Created by 流風幻葬 on 2017/3/30.
	*/
object BoxManager {
	
	def props(wsClient: ActorRef) = Props(new BoxManager(wsClient))
	
	case class InitDone(boxList: List[BoxInformation])
	case class SaveRequest(boxMac: String, shoots: List[rBasicShoot])
	case class ShootWithDistance(boxMac: String, clientMac: String, timestamp: Long, distance: Double)
	case class DistanceShoots(disShoots: List[ShootWithDistance])
	case class FileClientLocationList(locationList: List[FileClientLocation])
	case class GetDataFromFile(start: Long)
	case class CollectionFinish(send: ActorRef, date: String)
	case object RegularlyCounting
	case object WorkDone
	
	case class BoxInformation(boxMac: String, rssiSet: Int, distanceLoss: Double, referenceRSSI: Double, x: Double, y: Double, verticalHeight: Double)
	case class FileClientLocation(clientMac: String, timestamp: Long, x: Double, y: Double)
}

class BoxManager(wsClient: ActorRef) extends Actor with Stash{
	
	private[this] val log = LoggerFactory.getLogger(this.getClass)
	private[this] val logPrefix = context.self.path
	private[this] val selfRef = context.self
	private[this] val boxInfo = scala.collection.mutable.ListBuffer[BoxInformation]()
	private[this] val maxX = 1920.0
	private[this] val maxY = 488.0
	private[this] val distanceShoots = scala.collection.mutable.ListBuffer[ShootWithDistance]()
	private[this] var collectCount = 0
	
	def getInitMillis = {
		val delayTargetMinute = 1
		DateTime.now.plusMinutes(delayTargetMinute).withSecondOfMinute(0).getMillis - DateTime.now.getMillis
	}
	
	def getBoxWorker(boxMac: String, rssiSet: Int, distanceLoss: Double, referenceRSSI: Double, verticalHeight: Double): ActorRef = {
		context.child(boxMac).getOrElse {
			val child = context.actorOf(BoxWorker.props(boxMac, rssiSet, distanceLoss, referenceRSSI, verticalHeight), boxMac)
			log.info(s"From BoxManager: $logPrefix $boxMac is starting.")
			context.watch(child)
			child
		}
	}
	
	private[this] def isInRegion(point: (String, (Double, Double))): Boolean = {
		point._2._1 > 0.0 &&
		point._2._1 < maxX &&
		point._2._2 > 0.0 &&
		point._2._2 < maxY
	}
	
	private[this] def doublePoint(basePoint1: (Double, Double), basePoint2: (Double, Double), d1: Double, d2: Double) = {
		val (x1, y1) = basePoint1
		val (x2, y2) = basePoint2
		val totalLength = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
		val target1 = ((d1 / totalLength) * (x2 - x1) + x1 , (d1 / totalLength) * (y2 - y1) + y1)
		val target2 = ((d2 / totalLength) * (x1 - x2) + x2 , (d2 / totalLength) * (y1 - y2) + y2)
		((target1._1 + target2._1) / 2, (target1._2 + target2._2) / 2)
	}
	
	context.system.scheduler.schedule(
		getInitMillis millis,
		1 minute,
		self,
//		RegularlyCounting(DateTime.now.withTime(16,53,0,0).getMillis, DateTime.now.withTime(16,54,0,0).getMillis)
		RegularlyCounting
	)
	
	override def preStart = {
		log.info(s"$logPrefix is now starting.")
		context.setReceiveTimeout(2 minutes)
		BoxDAO.getAllBoxs.map { res =>
			selfRef ! InitDone(res.map(e => BoxInformation(e.boxMac, e.rssiSet, e.distanceLoss, e.rssiRefernce, e.x, e.y, e.verticalHeight)).toList)
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
				boxInfo.append(e)
				val actor = getBoxWorker(e.boxMac, e.rssiSet, e.distanceLoss, e.referenceRSSI, e.verticalHeight)
				wsClient ! SubscribeData(actor, e.boxMac)
			}
			unstashAll()
			context.become(working)
			
		case _ =>
			stash()
	}
	
	def working: Receive = {
		
		case SaveRequest(boxMac, shoots) =>
			context.become(busy)
			BasicShootDAO.addShoots(shoots).map {
				case Success(_) =>
					log.info(s"$boxMac insert shoots success, size: ${shoots.size}")
					selfRef ! WorkDone
				case Failure(e) =>
					log.error(s"$boxMac insert shoots FAILED: ${e.getMessage}")
					selfRef ! WorkDone
			}
			
		case RegularlyCounting =>
			val start = DateTime.now.minusMinutes(2).withSecondOfMinute(0).withMillisOfSecond(0).getMillis
			val end = DateTime.now.minusMinutes(1).withSecondOfMinute(0).withMillisOfSecond(0).getMillis
			BasicShootDAO.getShootsByTime(start, end).map{ list =>
				val distanceInfo = list.groupBy(_.clientMac).map{ userRecords => //clientMac -> records
					val records = userRecords._2.groupBy(_.boxMac).map{ e => // clientMac -> boxMac -> records
						val recordSize = e._2.size
						(e._1, e._2.map(_.distance).sum / recordSize)
					}
					/*if(records.size == 2) {
						val info1 = (boxInfo.getOrElse(records.head._1, (0.0, 0.0)), records.head._2)
						val info2 = (boxInfo.getOrElse(records.last._1, (0.0, 0.0)), records.last._2)
						(userRecords._1, doublePoint(info1._1, info2._1, info1._2, info2._2))
					}
					else */if(records.size >= 3) {
						val reg = new SimpleRegression(true)
						val x1 = boxInfo.filter(_.boxMac == records.head._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0))._1
						val y1 = boxInfo.filter(_.boxMac == records.head._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0))._2
						val d1 = records.head._2
						log.info(s"x1 y1:: $x1 $y1")
						records.drop(1).foreach { e =>
							val x = boxInfo.filter(_.boxMac == e._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0))._1
							val y = boxInfo.filter(_.boxMac == e._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0))._2
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
				}.filter(e => e._1 != "dummy" && isInRegion(e))
					.map(e => rClientLocation(-1, e._1, start, e._2._1, e._2._2)).toList
				ClientLocationDAO.addRecords(distanceInfo).map {
					case Success(num) =>
						log.info(s"insert client location success, size: $num")
					case Failure(e) =>
						log.error(s"insert client location FAILED: $e")
				}
			}
			
		case GetDataFromFile(start) =>
			log.info(s"GetDataFromFile: $start")
			val send = sender()
			val date = DateTime.now.withMillis(start).toString("yyyyMMdd")
			val fileName = s"data_$date.txt"
			val fileInfo = FileUtil.readAksoData(fileName)
			fileInfo.foreach { shoots =>
				collectCount = 0
				distanceShoots.clear
				val boxMacList = boxInfo.toList.map(_.boxMac).toSet
				val collectLimit = shoots.filter(e => boxMacList.apply(e.boxMac)).map(_.boxMac).toSet.size
				log.info(s"collectLimit: $collectLimit")
				shoots.filter(e => e.src != "error" && boxMacList.apply(e.boxMac))
					.groupBy(_.boxMac).foreach { e =>
					val boxSetting = boxInfo.filter(_.boxMac == e._1).head
					getBoxWorker(e._1, boxSetting.rssiSet, boxSetting.distanceLoss, boxSetting.referenceRSSI, boxSetting.verticalHeight) ! GetDistance(e._1, e._2)
				}
				log.info("now collecting")
				context.become(collecting(collectLimit, send, date))
			}
			
		case CollectionFinish(send, date) =>
			log.info("collect finish")
			val timeSplitList = scala.collection.mutable.HashMap[Long, List[ShootWithDistance]]()
			val format = DateTimeFormat.forPattern("yyyyMMdd")
			val startTime = DateTime.parse(date, format).getMillis
			val endTime = startTime + 24 * 60 * 60 * 1000 - 60 * 1000
			val stepTime = 60 * 1000
			log.info(s"$startTime ::: $endTime")
			for(t <- startTime to endTime by stepTime) {
				timeSplitList.put(t, distanceShoots.filter(e => e.timestamp >= t && e.timestamp < t + 60 * 1000).toList)
			}
			log.info(s"timeSplitList size: ${timeSplitList.size}")
			val allDistance = timeSplitList.filter(_._2.nonEmpty).flatMap { listWithTime =>
				val list = listWithTime._2
				list.groupBy(_.clientMac).map { userRecords => //clientMac -> records
					val records = userRecords._2.groupBy(_.boxMac).map { e => // clientMac -> boxMac -> records
						val recordSize = e._2.size
						(e._1, e._2.map(_.distance).sum / recordSize)
					}
					if(records.size == 2) {
					val info1 = (boxInfo.filter(_.boxMac == records.head._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0)), records.head._2)
					val info2 = (boxInfo.filter(_.boxMac == records.last._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0)), records.last._2)
					(userRecords._1, doublePoint(info1._1, info2._1, info1._2, info2._2))
				}
				else if (records.size >= 3) {
						val reg = new SimpleRegression(true)
						val x1 = boxInfo.filter(_.boxMac == records.head._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0))._1
						val y1 = boxInfo.filter(_.boxMac == records.head._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0))._2
						val d1 = records.head._2
						records.drop(1).foreach { e =>
							val x = boxInfo.filter(_.boxMac == e._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0))._1
							val y = boxInfo.filter(_.boxMac == e._1).map(e => (e.x, e.y)).headOption.getOrElse((0.0, 0.0))._2
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
				}.filter(e => e._1 != "dummy" && isInRegion(e))
					.map(e => FileClientLocation(e._1, listWithTime._1, e._2._1, e._2._2)).toList
			}.toList
			log.info(s"allDistance size : $allDistance")
			send ! FileClientLocationList(allDistance)
			
		case Terminated(child) =>
			log.error(s"$logPrefix child terminated: ${child.path.name} is dead.")
		
		case msg =>
			log.warn(s"$logPrefix get unknown message: $msg")
	}
	
	def collecting(collectLimit: Int, send: ActorRef, date: String): Receive = {
		case DistanceShoots(infoList) =>
			distanceShoots ++= infoList
			collectCount += 1
			if(collectCount >= collectLimit) {
				unstashAll()
				context.become(working)
				self ! CollectionFinish(send, date)
			}
	
		case _ =>
			stash()
	}
	
	def busy: Receive = {
		case WorkDone =>
			unstashAll()
			context.become(working)
			
		case _ =>
			stash()
	}
	
}
