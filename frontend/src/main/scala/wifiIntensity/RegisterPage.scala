package wifiIntensity

import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Div
import org.scalajs.dom.{KeyboardEvent, MouseEvent}
import wifiIntensity.ptcl._
import wifiIntensity.utils.{Http, Shortcut}
import scala.concurrent.ExecutionContext.Implicits.global


/**
	* Created by 流風幻葬 on 2017/2/9.
	*/
object RegisterPage extends Component[Div]{
	
	import scalatags.JsDom.short._
	import io.circe.generic.auto._
	import io.circe.syntax._
	
	val usernameDom = input(*.`type`:= "text", *.cls:= "form-control form-front", *.placeholder:= "用户名", *.id:= "username", *.required:= "", *.autofocus:= "").render
	val passwordDom = input(*.`type`:= "password", *.cls:= "form-control form-front", *.placeholder:= "密码", *.id:= "password", *.required:= "").render
	val passwordAgainDom = input(*.`type` := "password", *.cls:= "form-control form-last", *.placeholder := "确认密码", *.id:= "passwordAgain", *.required:= "").render
	val submitButton = button(*.cls:= "btn btn-lg btn-primary btn-block")("立即注册").render
	val returnButton = button(*.cls:= "btn btn-lg btn-primary btn-block")("返回登录").render
	
	submitButton.onclick = {e: MouseEvent =>
		e.preventDefault()
		val name = usernameDom.value
		val password = passwordDom.value
		val passwordAgain = passwordAgainDom.value
		
		if(name.isEmpty || password.isEmpty) {
			Shortcut.alert("信息尚未填写完整")
		}
		else if (password != passwordAgain) {
			Shortcut.alert("两次输入的密码不一致")
		}
		else {
			val body = RegisterReq(name, password).asJson.noSpaces
			Http.postJsonAndParse[CommonRsp](Routes.RegisterRoute.RegisterSubmit, body).map{
				case Right(rsp) =>
					if(rsp.errCode == 0) {
						val loginBody = LoginReq(name, password).asJson.noSpaces
						Http.postJsonAndParse[CommonRsp](Routes.LoginRoute.loginSubmit, loginBody).map{
							case Right(loginRsp) =>
								if(loginRsp.errCode == 0) {
									Shortcut.redirect(Routes.UserRoute.homeUrl)
								}
								else {
									Shortcut.alert(rsp.msg)
								}
							case Left(error) =>
								println("post admin login form error",error)
								Shortcut.alert("登录信息有误")
						}
					}
					else {
						Shortcut.alert(rsp.msg)
					}
					
				case Left(_) =>
					Shortcut.alert("register form error")
			}
		}
	}
	
	returnButton.onclick = { e:MouseEvent =>
		e.preventDefault()
		Shortcut.redirect(Routes.LoginRoute.loginUrl)
	}
	
	passwordAgainDom.onkeypress = { e: KeyboardEvent =>
		if(e.charCode == KeyCode.Enter) {
			submitButton.onclick
		}
	}
	
	override def render(): Div = {
		div(
			img(*.src:= "static/pic/bg.jpg", *.width:= "100%", *.height:= "100%", *.position:= "absolute", *.zIndex:= "-1", *.top:= "0px"),
			div(*.cls:= "wrapper")(
				form(*.cls:= "form-signin")(
					h2(*.cls:= "form-signin-heading", *.textAlign:= "center")("账号注册"),
					usernameDom,
					passwordDom,
					passwordAgainDom,
					submitButton,
					returnButton
				)
			)
		).render
	}
}
