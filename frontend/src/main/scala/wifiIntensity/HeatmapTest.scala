package wifiIntensity

import scalatags.JsDom.short._
import org.scalajs.dom.html.Div
import wifiIntensity.facade.heatmap.{Data, Point, Config}

import scala.scalajs.js.JSConverters._

/**
	* Created by 流風幻葬 on 2017/4/18.
	*/
object HeatmapTest extends Component[Div]{
	
	val map = div(*.id:= "heatmap", *.height:= "840px", *.width:= "400px").render
	var heatmapInstance = h337.create(new Config(map))
	val points = scala.collection.mutable.ListBuffer[Point]()
	var max = 0
	val width = 840
	val height = 400
	var len = 200
	
	for(_ <- 1 to len){
		val value = Math.floor(Math.random()*100).toInt
		max = if(max < value) value else max
		val point = new Point(
			Math.floor(Math.random()*width).toInt,
			Math.floor(Math.random()*height).toInt,
			value
		)
		points.append(point)
	}
	// heatmap data format
	val data = new Data(max, points.toJSArray)
	// if you have a set of datapoints always use setData instead of addData
	// for data initialization
	heatmapInstance.setData(data)
	
	override def render(): Div = {
		map
	}
}
