package wifiIntensity.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.slf4j.LoggerFactory
import wifiIntensity.http.SessionBase.UserSession
import wifiIntensity.protocol.ErrorCode
import wifiIntensity.ptcl.{CommonRsp, LoginReq, RegisterReq}
import wifiIntensity.models.dao.UserDAO
import wifiIntensity.utils.SecureUtil

import scala.concurrent.Future

/**
	* Created by 流風幻葬 on 2017/4/17.
	*/
trait AdminService extends HttpService{
	
	import io.circe.generic.auto._
	import io.circe._
	
	private[this] val log = LoggerFactory.getLogger(this.getClass)
	
	val loginPage = (path("login") & get) {
		getFromResource("html/index.html")
	}
	
	val loginSubmit = (path("loginSubmit") & post) {
		entity(as[Either[Error, LoginReq]]) {
			case Right(loginInfo) =>
				dealFutureResult {
					UserDAO.getUser(loginInfo.userName).map { user =>
						if(user.isDefined) {
							val userInfo = user.get
							val encrypted = SecureUtil.getSecurePassword(loginInfo.password, "127.0.0.1", userInfo.createTime)
							if(userInfo.password == encrypted) {
								val session = UserSession(userInfo.uid, userInfo.userName, System.currentTimeMillis)
								setUserSession(session) {
									complete(CommonRsp())
								}
							}
							else {
								complete(ErrorCode.wrongPassword)
							}
						}
						else {
							complete(ErrorCode.userNotExist)
						}
					}
				}
			case Left(error) =>
				log.warn(s"login format error: $error")
				complete(ErrorCode.jsonFormatError)
		}
	}
	
	val logout = (path("logout") & get) {
		invalidateSession{
			redirect("./login", StatusCodes.SeeOther)
		}
	}
	
	private val registerPage = (path("register") & get) {
		getFromResource("html/index.html")
	}
	
	val registerSubmit = (path("registerSubmit") & post) {
		entity(as[Either[Error, RegisterReq]]) {
			case Right(userInfo) =>
				dealFutureResult {
					UserDAO.getUser(userInfo.userName).flatMap { userOpt =>
						if(userOpt.isEmpty) {
							UserDAO.createUser(userInfo.userName, userInfo.password).map { res =>
								if (res > 0) {
									complete(CommonRsp())
								}
								else {
									complete(ErrorCode.createUserError)
								}
							}
						}
						else {
							Future(complete(ErrorCode.userExisted))
						}
					}
				}
			case Left(error) =>
				log.warn(s"login format error: $error")
				complete(ErrorCode.jsonFormatError)
		}
	}
	
	val loginRoutes = {
		loginSubmit ~ logout ~ loginPage
	}
	
	val registerRoutes = {
		registerPage ~ registerSubmit
	}
	
	val adminRoutes = {
		loginRoutes ~ registerRoutes
	}
	
}
