package wifiIntensity

import scalatags.JsDom.short._
import org.scalajs.dom.html.Div

/**
	* Created by 流風幻葬 on 2017/4/20.
	*/
object HeatmapTestPage extends Component[Div]{
	
	val picBox = div(*.id:= "heatmap", *.height:= "400px", *.width:= "600px").render
	val button = new HeatmapDrawer().render()
	override def render(): Div = {
		div(
			picBox,
			br,
			button
		).render
	}
}
