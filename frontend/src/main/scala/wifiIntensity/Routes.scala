package wifiIntensity

/**
  * User: Taoz
  * Date: 12/15/2016
  * Time: 3:05 PM
  */
object Routes {
  
  val baseUrl = "/wifiIntensity"
  
  object LoginRoute{
    val loginUrl = baseUrl + "/login"
    val loginSubmit = baseUrl + "/loginSubmit"
    val logoutUrl = baseUrl + "/logout"
  }
  
  object RegisterRoute{
    val RegisterUrl = baseUrl + "/register"
    val RegisterSubmit = baseUrl + "/registerSubmit"
  }
  
  object UserRoute{
    val userUrl = baseUrl + "/user"
    val homeUrl = userUrl + "/home"
    val getUserInfo = userUrl + "/getUserInfo"
    val getBox = userUrl + "/getBox"
    val addBox = userUrl + "/addBox"
    val getName = userUrl + "/getName"
    val deleteBox = userUrl + "/deleteBox"
    val uploadMap = userUrl + "/uploadMap"
  }
}
