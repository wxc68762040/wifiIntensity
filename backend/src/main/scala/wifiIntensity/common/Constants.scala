package wifiIntensity.common

/**
  * Created by liuziwei on 2016/7/10.
  */
object Constants {

  object Routes {
    val apiRoutes = "/nyx2/api"
    val adminRoutes = "/nyx2/admin"
    val testRoutes = "/nyx2/test"

  }

  //TODO 考虑实际情况的基础上尽量可以在配置文件里进行配置
  val defaultGroupId = 0l
  val defaultAreaId = 0l

  object Behavior {
    val all = "all"
    val in = "in"
    val out = "out"
  }

  object UserType {
    val all = "all"
    val customer = "customer"
    val staff = "staff"
  }

  object StayTimeStatus {
    val person = 0
    val personTime = 1
  }
}