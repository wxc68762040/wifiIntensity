package wifiIntensity

import org.scalajs.dom.raw.Element

import scalatags.JsDom.short._
import org.scalajs.dom.html.{Button, Input}
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLElement
import wifiIntensity.facade.heatmap._
import wifiIntensity.ptcl.{HeatDataReq, HeatDataRsp}
import wifiIntensity.utils.{Http, Shortcut}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.JSConverters._
import io.circe.generic.auto._
import io.circe.syntax._

/**
	* Created by 流風幻葬 on 2017/4/18.
	*/
class HeatmapDrawer(date: Input) extends Component[Button]{
	
	var firstChecker = 0
	val drawButton = button(*.cls:= "btn btn-primary",
		*.onclick:= { e: MouseEvent =>
		e.preventDefault()
		drawPic
		firstChecker = 1
	})("绘制热度图").render
	
	def drawPic: Unit = {
		val box = document.querySelector(".heatmap")
		if(firstChecker != 0) {
			val existedHeatmap = document.querySelector(".heatmap-canvas")
			box.removeChild(existedHeatmap)
		}
		val heatmapInstance = h337.create(new Config {
			override val container: js.UndefOr[Element] = box
			override val opacity: js.UndefOr[Double] = 0.3
		})
		val points = scala.collection.mutable.ListBuffer[Point]()
		var maxNum = 0
//		val width = box.clientWidth
//		val height = box.clientHeight
//		val len = 400
//
//		for (_ <- 1 to len) {
//			val randomValue = Math.floor(Math.random() * 100).toInt
//			maxNum = if (maxNum < randomValue) randomValue else maxNum
//			val point = new Point {
//				override val x: js.UndefOr[Int] = Math.floor(Math.random() * width).toInt
//				override val y: js.UndefOr[Int] = Math.floor(Math.random() * height).toInt
//				override val value: js.UndefOr[Int] = randomValue
//			}
//			points.append(point)
//		}
		val timestamp = Date.parse(date.value + " 00:00:00").toLong
		val body = HeatDataReq(timestamp, timestamp + 86400000).asJson.noSpaces
		Http.postJsonAndParse[HeatDataRsp](Routes.UserRoute.getHeatData, body).map {
			case Right(rsp) =>
				rsp.dataList.foreach { info =>
					if(maxNum < info.total) maxNum = info.total
					val point = new Point {
						override val x:js.UndefOr[Int] = info.x
						override val y:js.UndefOr[Int] = info.y
						override val value:js.UndefOr[Int] = info.total
					}
					points.append(point)
				}
				val data = new Data {
					override val max: js.UndefOr[Int] = maxNum
					override val min: js.UndefOr[Int] = 0
					override val data: js.UndefOr[js.Array[Point]] = points.toJSArray
				}
				heatmapInstance.setData(data)
			case Left(e) =>
				Shortcut.alert(s"draw heatmap error : $e")
		}
		// heatmap data format
//		val data = new Data {
//			override val max: js.UndefOr[Int] = maxNum
//			override val min: js.UndefOr[Int] = 0
//			override val data: js.UndefOr[js.Array[Point]] = points.toJSArray
//		}
		// if you have a set of datapoints always use setData instead of addData
		// for data initialization
	}
	
	override def render(): Button = {
		drawButton
	}
}
