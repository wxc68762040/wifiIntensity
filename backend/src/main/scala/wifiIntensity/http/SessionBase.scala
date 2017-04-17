package wifiIntensity.http

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import org.slf4j.LoggerFactory
import wifiIntensity.common.AppSettings
import wifiIntensity.protocol.ErrorCode
import wifiIntensity.utils.{CirceSupport, SessionSupport}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * User: Taoz
  * Date: 12/15/2016
  * Time: 10:49 AM
  */

object SessionBase {

  object SessionKeys {
    val uid = "uid"
    val userName = "username"
    val loginTime = "loginTime"
  }
  
  case class UserSession(
                          uid: Long,
                          name: String,
                          loginTime: Long
                        ) {
    def toSessionMap = Map(
      SessionKeys.uid -> uid.toString,
      SessionKeys.userName -> name,
      SessionKeys.loginTime -> loginTime.toString
    )
  }

  val log = LoggerFactory.getLogger(this.getClass)


}

trait SessionBase extends CirceSupport with SessionSupport {
  
  import SessionBase._
  import io.circe.generic.auto._

  override val sessionEncoder = SessionSupport.PlaySessionEncoder
  override val sessionConfig = AppSettings.sessionConfig
  private val timeout = 24 * 60 * 60 * 1000


  val userAuth = optionalSession.map {
    case Right(session) => parseSession(session)
    case Left(_) => None
  }

  protected def setUserSession(userSession: UserSession): Directive0 = setSession(userSession.toSessionMap)

  private def parseSession(session: Map[String, String]) = {
    (session.get(SessionKeys.userName), session.get(SessionKeys.loginTime)) match {
      case (Some(username), Some(ts)) =>
        Try {
          if (System.currentTimeMillis() - ts.toLong < timeout) {
            Some(username)
          } else {
            None
          }
        }.getOrElse(None)
      case _ => None
    }
  }

}