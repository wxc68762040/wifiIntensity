package wifiIntensity.utils

import akka.actor.{Actor, Props}
import wifiIntensity.common.AppSettings

/**
  * Created by ZYQ on 2016/6/1.
  **/
object AppClientKeys {

  def props = Props[AppClientKeys]

  final case class AskAppClientKey(appClientId: String)

  final case class AppClientSecureKey(key: Option[String])

}

class AppClientKeys extends Actor {

  import AppClientKeys._

  var keysMap = AppSettings.appSecureMap


  override def receive: Receive = {
    case AskAppClientKey(id) =>
      sender() ! AppClientSecureKey(keysMap.get(id))

  }
}