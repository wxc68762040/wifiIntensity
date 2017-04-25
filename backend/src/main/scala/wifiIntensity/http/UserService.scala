package wifiIntensity.http

import akka.http.scaladsl.server.Directives._
import org.slf4j.LoggerFactory
import wifiIntensity.models.dao.{BoxDAO, UserDAO}
import wifiIntensity.models.tables.rBoxs
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
							complete(UserInfoRsp(userInfo.get.userName, userInfo.get.file.get))
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
							val line = rBoxs(r.boxMac, r.boxName, -65, 2.1, r.x, r.y, uid.toLong)
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


	
	
	val userRoutes = pathPrefix("user"){
		home ~ getName ~ getUserInfo ~ getUserBox ~ addBox ~ deleteBox ~ uploadMap
	}
	
}
