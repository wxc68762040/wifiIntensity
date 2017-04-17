package wifiIntensity

import wifiIntensity.utils.{Http, Shortcut}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Div
import org.scalajs.dom.{KeyboardEvent, MouseEvent}
import wifiIntensity.ptcl._
import scala.concurrent.ExecutionContext.Implicits.global

/**
	* Created by 流風幻葬 on 2017/2/7.
	*/

object LoginPage extends Component[Div]{
	
	import scalatags.JsDom.short._
	import io.circe.generic.auto._
	import io.circe.syntax._
	
	val nameDom = input(*.`type` := "text", *.placeholder := "用户名", *.id := "name").render
	val passwordDom = input(*.`type` := "password", *.placeholder := "密码", *.id := "password").render
	
	val submitButton = button(*.cls:= "pure-button pure-button-primary")("登录").render
	
	submitButton.onclick = {e:MouseEvent =>
		e.preventDefault()
		val name = nameDom.value
		val password = passwordDom.value
		val body = LoginReq(name,password).asJson.noSpaces
		Http.postJsonAndParse[CommonRsp](Routes.LoginRoute.loginSubmit,body).map{
			case Right(rsp) =>
				if(rsp.errCode == 0) {
					Shortcut.redirect(Routes.UserRoute.homeUrl)
				}
				else Shortcut.alert(rsp.msg)
			case Left(error) =>
				println("post admin login form error",error)
				Shortcut.alert("登录信息有误")
		}
	}
	
	passwordDom.onkeypress = {e:KeyboardEvent =>
		if(e.charCode == KeyCode.Enter){
			e.preventDefault()
			submitButton.click()
		}
	}
	
	override def render(): Div = {
		div(*.cls:= "pure-g")(
			div(*.cls := "pure-u-1")(
				h1(*.textAlign := "center")("聊天室登录")
			),
			div(*.cls := "pure-u-1")(
				div(*.cls:= "pure-form", *.textAlign := "center")(
					table(*.style:= "margin:auto")(
						tr(
							td(label("用户名")),td(nameDom)
						),
						tr(
							td(label("密码")),td(passwordDom)
						),
						tr(
							td(submitButton)
						)
					)
				)
			)
		).render
	}
	
}