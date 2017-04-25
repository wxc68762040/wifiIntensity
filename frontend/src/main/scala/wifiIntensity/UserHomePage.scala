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
		"test1" -> ("测试1", div().render),
		"test2" -> ("测试2", div().render),
		"test3" -> ("测试3", div().render)
	)
	
//	val menuButton = a(*.href:= "#menu", *.id:="menuLink", *.cls:= "menu-link")(span)
	val nameBox = div(*.cls:= "name-box").render
	
	def switchRightBox(boxName: String) = { e: MouseEvent =>
		e.preventDefault()
		val box = menuMap.get(boxName).map(_._2).getOrElse(div(h1(s"cannot find box $boxName")).render)
		mainBox.textContent = ""
		mainBox.appendChild(box)
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
	
	val mainBox = new UserInfoBox().render()
	
	val topNaviBox =
		div(*.cls:= "navbar navbar-default", *.role:="navigation")(
			div(*.cls:= "container-fluid")(
				div(*.cls:= "navbar-header")(
					a(*.cls:= "navbar-brand", *.href:= "#")("热点分析系统"),
					span(*.cls:= "navbar-brand")(nameBox, getUserName)
				),
				div(
					ul(*.cls:= "nav navbar-nav")(
						menuMap.head match { case (boxName, (displayName, _)) =>
							val ele = a(*.name:= boxName,*.href:= "")(displayName).render
							ele.onclick = switchRightBox(ele.getAttribute("name"))
							li(*.cls:= "active")(ele).render
						},
						menuMap.tail.toList.map { case (boxName, (displayName, _)) =>
							val ele = a(*.name:= boxName,*.href:= "")(displayName).render
							ele.onclick = switchRightBox(ele.getAttribute("name"))
							li(ele).render
						}
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
