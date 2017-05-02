package wifiIntensity

import scalatags.JsDom.short._
import org.scalajs.dom.html.Div

/**
	* Created by 流風幻葬 on 2017/4/20.
	*/
object HeatmapTestPage extends Component[Div]{
	
	val picBox = div(*.id:= "heatmap", *.height:= "488px", *.width:= "1920px")(
		iframe(*.src:= "/wifiIntensity/static/pic/12map.svg", *.height:= "488px", *.width:= "1920px").render
	).render
	val datePicker = input(*.cls:= "form-control", *.`type` := "text", *.value := "2017-04-27",*.onclick:= s"WdatePicker({readOnly:true})", *.id := "myDatePicker").render
	val fromFile = select(*.id:= "fromFile", *.cls:= "form-control", option("从文件读取", *.value:= "1"), option("不从文件读取", *.value:= "0")).render
	val button = new HeatmapDrawer(datePicker, fromFile).render()
	override def render(): Div = {
		div(
			form(*.role:= "form")(
				div(*.cls:= "form-group")(
					datePicker,
					fromFile
				)
			),
			picBox,
			br,
			button
		).render
	}
}
