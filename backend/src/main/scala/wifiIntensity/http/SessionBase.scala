package wifiIntensity.http

import org.slf4j.LoggerFactory
import wifiIntensity.common.AppSettings
import wifiIntensity.utils.SessionSupport

import scala.util.Try

/**
  * User: Taoz
  * Date: 12/15/2016
  * Time: 10:49 AM
  */

object SessionBase {

  object SessionKeys {
    val username = "nyx_username"
    val timestamp = "nyx_timestamp"
  }

  val log = LoggerFactory.getLogger(this.getClass)


}

trait SessionBase extends SessionSupport {

  import SessionBase._

  override val sessionEncoder = SessionSupport.PlaySessionEncoder
  override val sessionConfig = AppSettings.sessionConfig
  private val timeout = 24 * 60 * 60 * 1000


  val userAuth = optionalSession.map {
    case Right(session) => parseSession(session)
    case Left(_) => None
  }


  private def parseSession(session: Map[String, String]) = {
    (session.get(SessionKeys.username), session.get(SessionKeys.timestamp)) match {
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