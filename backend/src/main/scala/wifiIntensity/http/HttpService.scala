package wifiIntensity.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  * Created by ZYQ on 2016/12/13.
  **/
trait HttpService
  extends BaseService
  with ResourceService
  with TestService
  with TodoService
  with AdminService
  with UserService
{

  val routes : Route = pathPrefix("wifiIntensity")(
    resourceRoutes ~ todoRoute ~ testRoutes ~ adminRoutes ~ userRoutes
  )
  
}
