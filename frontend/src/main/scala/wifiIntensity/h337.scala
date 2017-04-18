package wifiIntensity

import wifiIntensity.facade.heatmap.Config

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/**
	* Created by 流風幻葬 on 2017/4/18.
	*/

@js.native
@JSGlobal
object h337 extends js.Object{
	def create(config: Config) : heatmapInstance = js.native
}

@js.native
@JSGlobal
class heatmapInstance extends js.Object{
	def setData(data: js.Object): js.Any = js.native
}