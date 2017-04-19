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
object HeatmapTest extends Component[Div]{
	
	val map = div(*.id:= "heatmap", *.style:= "width:600px; height:400px").render
	
	def draw: Unit = {
		val heatmapInstance = h337.create(new Config{override val container:js.UndefOr[Element] = map})
		val points = scala.collection.mutable.ListBuffer[Point]()
		var maxNum = 0
		val width = 840
		val height = 400
		val len = 200
		
		for(_ <- 1 to len){
			val randomValue = Math.floor(Math.random()*100).toInt
			maxNum = if(maxNum < randomValue) randomValue else maxNum
			val point = new Point {
				override val x:js.UndefOr[Int] = Math.floor(Math.random() * width).toInt
				override val y:js.UndefOr[Int] = Math.floor(Math.random() * height).toInt
				override val value:js.UndefOr[Int] = randomValue
			}
			points.append(point)
		}
		// heatmap data format
		val data = new Data{
			override val max: js.UndefOr[Int] = maxNum
			override val min: js.UndefOr[Int] = 0
			override val data: js.UndefOr[js.Array[Point]] = points.toJSArray
		}
		println("~~~", data.max)
		// if you have a set of datapoints always use setData instead of addData
		// for data initialization
		heatmapInstance.setData(data)
	}
	
	override def render(): Div = {
		println("!!!!!!!!!!!!")
		val a =div(
			div(*.height:= 200, *.width:= 600, *.backgroundColor:= "red"),
			div(*.height:= 400, *.width:= 600)(map)
		).render
		draw
		a
	}
}
