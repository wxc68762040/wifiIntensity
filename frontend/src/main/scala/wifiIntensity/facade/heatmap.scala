package wifiIntensity.facade

import org.scalajs.dom.raw.Element
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js
import js.annotation.{JSGlobal, JSName, ScalaJSDefined}

/**
	* Created by 流風幻葬 on 2017/4/18.
	*/
object heatmap {
	
	@js.native
	@JSGlobal("h337")
	object h337 extends js.Object{
		def create(config: Config) : HeatmapInstance = js.native
	}
	
	@js.native
	@JSGlobal("Heatmap")
	class HeatmapInstance(config: Config) extends js.Object{
		def setData(data: Data): Unit = js.native
	}
	
	@ScalaJSDefined
	trait Point extends js.Object{
		val x: js.UndefOr[Int] = js.undefined
		val y: js.UndefOr[Int] = js.undefined
		val value: js.UndefOr[Int] = js.undefined
	}
	
	@ScalaJSDefined
	trait Data extends js.Object{
		val max: js.UndefOr[Int] = js.undefined
		val min: js.UndefOr[Int] = js.undefined
		val data: js.UndefOr[js.Array[Point]] = js.undefined
	}
	
	@ScalaJSDefined
	trait Config extends js.Object{
		val	container: js.UndefOr[Element] = js.undefined
	}
}
