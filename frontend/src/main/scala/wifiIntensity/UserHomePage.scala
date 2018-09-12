package wifiIntensity

import wifiIntensity.utils.{Http, Shortcut}
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.html.Div
import wifiIntensity.ptcl.SessionNameRsp
import scala.concurrent.ExecutionContext.Implicits.global

/**
	* Created by 流風幻葬 on 2017/4/24.
	*/
object UserHomePage extends Component[Div]{
	
	import scalatags.JsDom.short._
	import io.circe.generic.auto._
	import io.circe.syntax._
	
	val menuMap = Map(
		"basicInfoShow" -> ("用户信息", new UserInfoBox().render()),
		"heatmap" -> ("热度分析", new HeatmapBox().render())
	)
	
	val nameBox = div(*.cls:= "name-box").render
	
	def switchRightBox(boxName: String) = { e: MouseEvent =>
		e.preventDefault()
		val box = menuMap.get(boxName).map(_._2).getOrElse(div(h1(s"cannot find box $boxName")).render)
		mainBox.replaceChild(box, mainBox.firstChild)
	}
	
	def getUserName: Unit = {
		Http.getAndParse[SessionNameRsp](Routes.UserRoute.getName).map {
			case Right(rsp) =>
				nameBox.appendChild(span("用户: ", rsp.username).render)
			
			case Left(e) =>
				Shortcut.alert("get admin name error!")
				nameBox.appendChild(span("???").render)
		}
	}
	
	val mainBox = div(new UserInfoBox().render()).render
	
	val topNaviBox =
		div(*.cls:= "navbar navbar-default", *.role:="navigation")(
			div(*.cls:= "container-fluid")(
				div(*.cls:= "navbar-header")(
					a(*.cls:= "navbar-brand", *.href:= "#")("热点分析系统"),
					span(*.cls:= "navbar-brand")(nameBox, getUserName)
				),
				div(
					ul(*.cls:= "nav navbar-nav navbar-left")(
						menuMap.toList.map { case (boxName, (displayName, _)) =>
							val ele = a(*.name:= boxName,*.href:= "")(displayName).render
							ele.onclick = switchRightBox(ele.getAttribute("name"))
							li(ele).render
						}
					),
					ul(*.cls:= "nav navbar-nav navbar-right")(
						li(a(*.href:= Routes.LoginRoute.logoutUrl)("注销"))
					)
				)
			)
		)
	
	override def render(): Div = {
		div(
			topNaviBox,
			mainBox
		).render
	}
}
