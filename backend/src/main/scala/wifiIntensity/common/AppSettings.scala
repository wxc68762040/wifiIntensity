package wifiIntensity.common

import java.io.File
import java.util.concurrent.TimeUnit

import wifiIntensity.utils.SessionSupport.SessionConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

/**
  * Created by liuziwei on 2016/7/4.
  * Updated by zhangtao on 2016/12/15
  */
object AppSettings {

  private implicit class RichConfig(config: Config) {
    val noneValue = "none"

    def getOptionalString(path: String): Option[String] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getString(path))

    def getOptionalLong(path: String): Option[Long] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getLong(path))

    def getOptionalDurationSeconds(path: String): Option[Long] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getDuration(path, TimeUnit.SECONDS))
  }


  val log = LoggerFactory.getLogger(this.getClass)
  val config = ConfigFactory.parseResources("product.conf").withFallback(ConfigFactory.load())


  val httpInterface = config.getString("app.http.interface")
  val httpPort = config.getInt("app.http.port")

  val appConfig = config.getConfig("app")
  val dataPath = appConfig.getString("dataPath")

  val aksoConfig = appConfig.getConfig("akso")
  val aksoAppId = aksoConfig.getString("appId")
  val aksoSecureKey = aksoConfig.getString("secureKey")
  val aksoHost = aksoConfig.getString("host")
  val aksoPort = aksoConfig.getString("port")

  val slickConfig = config.getConfig("slick.db")
  val slickUrl = slickConfig.getString("url")
  val slickUser = slickConfig.getString("user")
  val slickPassword = slickConfig.getString("password")
  val slickMaximumPoolSize = slickConfig.getInt("maximumPoolSize")
  val slickConnectTimeout = slickConfig.getInt("connectionTimeout")
  val slickIdleTimeout = slickConfig.getInt("idleTimeout")
  val slickMaxLifetime = slickConfig.getInt("maxLifetime")

  val appSecureMap = {
    import collection.JavaConversions._
    val appIdList = appConfig.getStringList("client.appIds")
    val secureKeys = appConfig.getStringList("client.secureKeys")
    require(appIdList.length == secureKeys.length, "appIdList.length and secureKeys.length not equal.")
    appIdList.zip(secureKeys).toMap
  }


  val sessionConfig = {
    val sConf = config.getConfig("session")
    SessionConfig(
      cookieName = sConf.getString("cookie.name"),
      serverSecret = sConf.getString("serverSecret"),
      domain = sConf.getOptionalString("cookie.domain"),
      path = sConf.getOptionalString("cookie.path"),
      secure = sConf.getBoolean("cookie.secure"),
      httpOnly = sConf.getBoolean("cookie.httpOnly"),
      maxAge = sConf.getOptionalDurationSeconds("cookie.maxAge"),
      sessionEncryptData = sConf.getBoolean("encryptData")
    )
  }



//  val username = appConfig.getString("login.username")
//  val password = appConfig.getString("login.password")


}