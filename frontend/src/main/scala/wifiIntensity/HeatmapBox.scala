package wifiIntensity

import wifiIntensity.utils.{Http, Shortcut}
import org.scalajs.dom.html.Div
import wifiIntensity.ptcl._

import scalatags.JsDom.short._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global

/**
	* Created by 流風幻葬 on 2017/4/26.
	*/
class HeatmapBox extends Component[Div]{
	val button = new HeatmapDrawer().render()
	val mapBoxWrapper = div(*.cls:= "heatmap-wrapper", *.overflow:= "auto").render
	val mapBox = div(*.cls:= "heatmap")().render
	val dateDom = input(*.`type`:= "date").render
	
	def getMap: Unit = {
		mapBoxWrapper.appendChild(mapBox)
		Http.getAndParse[UserInfoRsp](Routes.UserRoute.getUserInfo).map {
			case Right(rsp) =>
				if(rsp.errCode == 0) {
					val map = img(*.src := s"/wifiIntensity/static/uploadPic/${rsp.file}").render
					mapBox.appendChild(map)
					mapBoxWrapper.setAttribute("style", s"width: ${rsp.width}px; height:${rsp.height}px;")
				} else {
					Shortcut.alert(rsp.msg)
				}
			case Left(e) =>
				Shortcut.alert(s"get map error: $e")
		}
	}
	
	override def render(): Div = {
		div(
			mapBoxWrapper, getMap,
			button
		).render
	}
}
