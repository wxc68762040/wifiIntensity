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
	* Created by 流風幻葬 on 2017/4/18.
	*/
trait TestService extends BaseService{
	val testPage = (path("test") & get) {
		getFromResource("html/index.html")
	}
	
	val testRoutes = pathPrefix("anotherTest"){
		testPage
	}
}
