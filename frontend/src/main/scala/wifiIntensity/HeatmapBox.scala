package wifiIntensity

import wifiIntensity.utils.{Http, Shortcut}
import org.scalajs.dom.html.Div
import wifiIntensity.ptcl._

import scalatags.JsDom.short._
import io.circe.generic.auto._
import org.scalajs.dom.Event

import scala.concurrent.ExecutionContext.Implicits.global

/**
	* Created by 流風幻葬 on 2017/4/26.
	*/
class HeatmapBox extends Component[Div]{
	val mapBoxWrapper = div(*.cls:= "heatmap-wrapper", *.overflow:= "auto").render
	val mapBox = div(*.cls:= "heatmap")().render
	val dateLabel = label(*.`for`:= "datePicker")("日期：")
	val datePicker = input(*.cls:= "form-control", *.`type` := "date", *.id:= "datePicker",
		*.value := "2017-04-27", *.onclick:= s"WdatePicker({readOnly:true})", *.marginRight:= "30px").render
	val fromFile = select(*.id:= "fromFile", *.cls:= "form-control", option("从文件读取", *.value:= "1"), option("不从文件读取", *.value:= "0")).render
	val button = new HeatmapDrawer(datePicker, fromFile).render()
	
	datePicker.onchange = { e: Event =>
		e.preventDefault()
		if(datePicker.value.isEmpty) {
			button.setAttribute("disabled", "disabled")
		} else {
			button.removeAttribute("disabled")
		}
	}
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
			div(*.marginBottom:= "50px", *.cls:= "container")(
				form(*.cls:= "form-inline")(
					dateLabel, datePicker, fromFile, button
				)
			),
			div(
				mapBoxWrapper, getMap
			)
		).render
	}
}
