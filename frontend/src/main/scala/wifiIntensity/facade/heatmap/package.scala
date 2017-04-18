package wifiIntensity.facade

import org.scalajs.dom.raw.{Element, HTMLElement}

import scala.scalajs.js
import js.annotation.{JSName, ScalaJSDefined}

/**
	* Created by 流風幻葬 on 2017/4/18.
	*/
package object heatmap {
	
	@ScalaJSDefined
	class Point(
		val x: Int,
		val y: Int,
		val value: Int
	) extends js.Object

	@ScalaJSDefined
	class Data(
		max: Int,
		data: js.Array[Point]
	) extends js.Object
	
	@ScalaJSDefined
	class Config(
		container: Element
	) extends js.Object
}
