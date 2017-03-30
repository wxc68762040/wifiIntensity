package wifiIntensity.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  * Created by ZYQ on 2016/12/13.
  **/
trait HttpService
  extends BaseService
  with TodoService{

  val routes : Route = pathPrefix("wifiIntensity")(todoRoute)
  
}
