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
	
	val usernameDom = input(*.`type`:= "text", *.cls:= "form-control form-front", *.placeholder:= "用户名", *.id:= "username", *.required:= "", *.autofocus:= "").render
	val passwordDom = input(*.`type`:= "password", *.cls:= "form-control form-last", *.placeholder:= "密码", *.id:= "password", *.required:= "").render
	val submitButton = button(*.`type`:= "submit", *.cls:= "btn btn-lg btn-primary btn-block")("登录").render
	val registerButton = button(*.`type`:= "submit", *.cls:= "btn btn-lg btn-primary btn-block")("注册").render
	
	submitButton.onclick = {e:MouseEvent =>
		e.preventDefault()
		val name = usernameDom.value
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
	
	registerButton.onclick = {e:MouseEvent =>
		e.preventDefault()
		Shortcut.redirect(Routes.RegisterRoute.RegisterUrl)
	}
	
	override def render(): Div = {
		div(
			img(*.src:= "static/pic/bg.jpg", *.width:= "100%", *.height:= "100%", *.position:= "absolute", *.zIndex:= "-1", *.top:= "0px"),
			div(*.cls:= "wrapper")(
				form(*.cls:= "form-signin")(
					h2(*.cls:= "form-signin-heading", *.textAlign:= "center")("热度分析系统"),
					usernameDom,
					passwordDom,
					submitButton,
					registerButton
				)
			)
		).render
	}
	
}