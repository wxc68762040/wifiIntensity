package wifiIntensity.http

import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import org.slf4j.LoggerFactory
import wifiIntensity.core.BoxManager.{FileClientLocation, FileClientLocationList, GetDataFromFile}
import wifiIntensity.models.dao.{BoxDAO, ClientLocationDAO, UserDAO}
import wifiIntensity.models.tables.{rBoxs, rClientLocation}
import wifiIntensity.protocol.ErrorCode
import wifiIntensity.ptcl._
import wifiIntensity.utils.FileUtil._

import scala.util.{Failure, Success}

/**
	* Created by 流風幻葬 on 2017/2/16.
	*/
trait UserService extends BaseService{
	
	import io.circe._
	import io.circe.generic.auto._
	
	private val log = LoggerFactory.getLogger(this.getClass)
	
	private val home = (path("home") & get){
		userAuth { session =>
			getFromResource("html/index.html")
		}
	}
	
	private val getName = (path("getName") & get){
		userAuth {
			case Some((_, username)) =>
				complete(SessionNameRsp(username))
			case None =>
				complete(ErrorCode.notLogin)
		}
	}
	
	private val getUserInfo = (path("getUserInfo") & get){
		userAuth {
			case Some((uid, _)) =>
				dealFutureResult {
					UserDAO.getUserByUid(uid.toLong).map { userInfo =>
						if (userInfo.nonEmpty) {
							val info = userInfo.get
							complete(UserInfoRsp(info.userName, info.file.get, info.width.get, info.height.get))
						} else {
							complete(ErrorCode.userNotExist)
						}
					}
				}
			case None =>
				complete(ErrorCode.notLogin)
		}
	}
	
	private val getUserBox = (path("getBox") & get){
		userAuth {
			case Some((uid, _)) =>
				dealFutureResult {
					BoxDAO.getBoxsByOwner(uid.toLong).map { lines =>
						val boxList = lines.map(e => BoxInfo(e.boxMac, e.boxName, e.x, e.y))
						complete(BoxListRsp(boxList.toList))
					}
				}
			case None =>
				complete(ErrorCode.notLogin)
		}
	}
	
	private val addBox = (path("addBox") & post) {
		entity(as[Either[Error, AddBoxReq]]) {
			case Right(r) =>
				userAuth {
					case Some((uid, _)) =>
						dealFutureResult {
							val line = rBoxs(r.boxMac, r.boxName, -65, 2.1, r.x, r.y, uid.toLong, r.verticalHeight)
							BoxDAO.addBox(line).map {
								case Success(_) =>
									complete(CommonRsp())
								case Failure(e) =>
									log.error(s"add box error: $e")
									complete(ErrorCode.addBoxError)
							}
						}
					case None =>
						complete(ErrorCode.notLogin)
				}
			case Left(_) =>
				complete(ErrorCode.jsonFormatError)
		}
	}
	
	private val deleteBox = (path("deleteBox") & post) {
		entity(as[Either[Error, DeleteBoxReq]]) {
			case Right(r) =>
				userAuth {
					case Some((_, _)) =>
						dealFutureResult {
							BoxDAO.deleteBox(r.boxMac).map {
								case Success(_) =>
									complete(CommonRsp())
								case Failure(e) =>
									log.error(s"delete box error: $e")
									complete(ErrorCode.deleteBoxError)
							}
						}
					case None =>
						complete(ErrorCode.notLogin)
				}
			case Left(_) =>
				complete(ErrorCode.jsonFormatError)
		}
	}
	
	private val uploadMap = (path("uploadMap") & post) {
		uploadedFile("fileUpload") {
			case (metadata, tmpFile) =>
				userAuth {
					case Some((uid, _)) =>
						log.debug(s"file upload, metadata:${metadata.toString}")
						val extName = getExtName(metadata.fileName).getOrElse("")
						val destFile = storeTmpFile(tmpFile, extName)
						dealFutureResult {
							UserDAO.uploadMap(uid.toLong, destFile.getName).map {
								case Success(_) =>
									complete(UploadMapRsp(destFile.getName))
								case Failure(e) =>
									log.error(s"uploadFile error: $e")
									complete(ErrorCode.uploadFileError)
							}
						}
					case None =>
						complete(ErrorCode.notLogin)
				}
		}
	}
	
	private val uploadSize = (path("uploadSize") & post) {
		entity(as[Either[Error, UploadSizeReq]]) {
			case Right(req) =>
				userAuth {
					case Some((uid, _)) =>
						dealFutureResult {
							UserDAO.uploadSize(uid.toLong, req.width, req.height).map {
								case Success(_) =>
									complete(CommonRsp())
								case Failure(e) =>
									log.error(s"uploadFile error: $e")
									complete(ErrorCode.uploadFileError)
							}
						}
					case None =>
						complete(ErrorCode.notLogin)
				}
			case Left(e) =>
				complete(ErrorCode.jsonFormatError)
		}
	}

	private val heatData = (path("heatData") & post) {
		entity(as[Either[Error, HeatDataReq]]) {
			case Right(req) =>
				if(req.fromFile == 0) {
					dealFutureResult {
						ClientLocationDAO.getRecordsByTime(req.start, req.end).map { rst =>
							val records = scala.collection.mutable.ListBuffer[rClientLocation]()
							val mergedData = scala.collection.mutable.ListBuffer[HeatData]()
							records ++= rst
							while (records.nonEmpty) {
								val sample = records.head
								val xRange = sample.x.toInt / 10 * 10
								val yRange = sample.y.toInt / 10 * 10
								val rangedRecords = records
									.filter(e => e.x >= xRange && e.x < xRange + 10 && e.y >= yRange && e.y < yRange + 10)
								records --= rangedRecords
								val total = rangedRecords.size
								val xCenter = rangedRecords.map(_.x).sum / total
								val yCenter = rangedRecords.map(_.y).sum / total
								mergedData += HeatData(xCenter.toInt, yCenter.toInt, total)
							}
							complete(HeatDataRsp(mergedData.toList))
						}
					}
				} else {
					dealFutureResult {
						(boxManager ? GetDataFromFile(req.start)).map {
							case FileClientLocationList(list) =>
								val records = scala.collection.mutable.ListBuffer[FileClientLocation]()
								val mergedData = scala.collection.mutable.ListBuffer[HeatData]()
								records ++= list
								while (records.nonEmpty) {
									val sample = records.head
									val xRange = sample.x.toInt / 10 * 10
									val yRange = sample.y.toInt / 10 * 10
									val rangedRecords = records
										.filter(e => e.x >= xRange && e.x < xRange + 10 && e.y >= yRange && e.y < yRange + 10)
									records --= rangedRecords
									val total = rangedRecords.size
									val xCenter = rangedRecords.map(_.x).sum / total
									val yCenter = rangedRecords.map(_.y).sum / total
									mergedData += HeatData(xCenter.toInt, yCenter.toInt, total)
								}
								complete(HeatDataRsp(mergedData.toList))
						}
					}
				}
			case Left(e) =>
				complete(ErrorCode.jsonFormatError)
		}
	}
	
	
	val userRoutes = pathPrefix("user"){
		home ~ getName ~ getUserInfo ~ getUserBox ~ addBox ~ deleteBox ~ uploadMap ~ uploadSize ~ heatData
	}
	
}
