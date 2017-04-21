package wifiIntensity

import org.scalajs.dom.raw.Element

import scalatags.JsDom.short._
import org.scalajs.dom.html.Div
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLElement
import wifiIntensity.facade.heatmap._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
	* Created by 流風幻葬 on 2017/4/18.
	*/
class HeatmapDrawer extends Component[Div]{
	
	var firstChecker = 0
	val drawButton = button(*.onclick:= { e: MouseEvent =>
		e.preventDefault()
		drawPic
		firstChecker = 1
	})("绘制热度图")
	
	def drawPic: Unit = {
		val box = document.querySelector("#heatmap")
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
		val width = box.clientWidth
		val height = box.clientHeight
		val len = 200
		
		for (_ <- 1 to len) {
			val randomValue = Math.floor(Math.random() * 100).toInt
			maxNum = if (maxNum < randomValue) randomValue else maxNum
			val point = new Point {
				override val x: js.UndefOr[Int] = Math.floor(Math.random() * width).toInt
				override val y: js.UndefOr[Int] = Math.floor(Math.random() * height).toInt
				override val value: js.UndefOr[Int] = randomValue
			}
			points.append(point)
		}
		// heatmap data format
		val data = new Data {
			override val max: js.UndefOr[Int] = maxNum
			override val min: js.UndefOr[Int] = 0
			override val data: js.UndefOr[js.Array[Point]] = points.toJSArray
		}
		// if you have a set of datapoints always use setData instead of addData
		// for data initialization
		heatmapInstance.setData(data)
	}
	
	override def render(): Div = {
		div(drawButton).render
	}
}
