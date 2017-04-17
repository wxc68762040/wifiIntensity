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
  
  object UserRoute{
    val homeUrl = baseUrl + "/home"
  }
}
