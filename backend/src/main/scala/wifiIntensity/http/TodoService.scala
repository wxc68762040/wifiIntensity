package wifiIntensity.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import org.slf4j.LoggerFactory

/**
	* Created by 流風幻葬 on 2017/3/30.
	*/
trait TodoService extends BaseService{
	
	val log = LoggerFactory.getLogger(this.getClass())
	val home: Route = (path("home") & get) {
		userAuth {
			case Some((_, username)) =>
				log.info(s"[$username to home].")
				getFromResource("html/index.html")
			case None =>
				log.info(s"you need login, redirect to login page.")
				redirect("./login", StatusCodes.SeeOther)
		}
	}
	
	val todoRoute: Route = pathPrefix("todo") {
		home
	}
}
