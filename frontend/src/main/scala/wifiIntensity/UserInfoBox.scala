package wifiIntensity

import io.circe.{Decoder, Error}
import wifiIntensity.utils.{Http, Shortcut}
import org.scalajs.dom.{Event, FormData, MouseEvent, XMLHttpRequest}
import org.scalajs.dom.html.Div
import org.scalajs.dom.document
import wifiIntensity.ptcl._

import scalatags.JsDom.short._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
	* Created by 流風幻葬 on 2017/4/25.
	*/
class UserInfoBox extends Component[Div]{
	val basicInfoDom = div().render
	val tableDom = table(*.cls:= "table table-striped")(
		caption(h2(*.textAlign:="center")("探测源信息"))
	).render
	val tableHead = thead(tr(
		td("探测源Mac"),
		td("探测源名称"),
		td("X坐标"),
		td("Y坐标"),
		td("操作")
	)).render
	val tableBody = tbody().render
	val mapDom = div(div()).render
	val file = input(*.`type` := "file", *.name := "fileUpload").render
	val fileUpload =
		form(*.enctype := "multipart/form-data", *.method := "post", *.marginBottom := "30px")(
			file
		).render
	file.onchange = {
		e:Event=>
			uploadImg()
	}
	
	def deleteBox(boxMac: String, index: Int) = {
		val body = DeleteBoxReq(boxMac).asJson.noSpaces
		Http.postJsonAndParse[CommonRsp](Routes.UserRoute.deleteBox, body).map {
			case Right(rsp) =>
				Shortcut.alert("删除成功")
				tableDom.deleteRow(index)
			case Left(error) =>
				println("delete room error", error)
				Shortcut.alert("delete room error " + error.getMessage)
		}
	}
	
	def getDeleteBoxBtn(boxMac: String, index: Int) = {
		button (
			*.cls:= "btn btn-danger",
			*.onclick:= { e:MouseEvent =>
				e.preventDefault()
				deleteBox(boxMac, index)
			}
		)("删除").render
	}
	
	def getUserBasicInfo: Unit =
		Http.getAndParse[UserInfoRsp](Routes.UserRoute.getUserInfo).map{
			case Right(rsp) =>
				if(rsp.errCode == 0) {
					val nameDom = div(
						span(s"用户名： ${rsp.username}")
					).render
					val mapPic = img(*.src:= s"/wifiIntensity/static/uploadPic/${rsp.file}").render
					basicInfoDom.appendChild(nameDom)
					mapDom.replaceChild(mapPic, mapDom.firstChild)
				} else {
					Shortcut.alert(rsp.msg)
				}
			
			case Left(e) =>
				Shortcut.alert(s"get basic info error: $e")
		}
	
	def getBoxList: Unit = {
		Http.getAndParse[BoxListRsp](Routes.UserRoute.getBox).map {
			case Right(rsp) =>
				if (rsp.errCode == 0) {
					tableBody.textContent = ""
					val boxInfoList = rsp.boxList
					boxInfoList.sortBy(_.boxMac).indices.foreach { index =>
						val box = boxInfoList(index)
						val boxDom = tr(
							td(box.boxMac),
							td(box.boxName),
							td(box.x),
							td(box.y),
							td(getDeleteBoxBtn(box.boxMac, index + 1))
						).render
						tableBody.appendChild(boxDom)
					}
					tableDom.appendChild(tableHead)
					tableDom.appendChild(tableBody)
				} else {
					Shortcut.alert(rsp.msg)
				}
			case Left(_) =>
				Shortcut.alert("get room list error")
		}
	}
	
	private def Parse[T](bodyStr: Future[String])(implicit decoder: Decoder[T]): Future[Either[Error, T]] = {
		import io.circe.parser._
		bodyStr.map(s => decode[T](s))
	}
	
	private def uploadImg() = {
		val oData = new FormData(fileUpload)
		println(s"oData = $oData")
		val oReq = new XMLHttpRequest()
		oReq.open("POST", Routes.UserRoute.uploadMap, true)
		oReq.send(oData)
		oReq.onreadystatechange = { e: Event =>
			if (oReq.readyState == 4) {
				if (oReq.status == 200) {
					val message = Future(oReq.responseText)
					Parse[UploadMapRsp](message).map {
						case Right(info) =>
							if (info.errCode == 0) {
								val imgUrl = s"/wifiIntensity/static/uploadPic/${info.mapPath}"
								val image = img(*.src := imgUrl).render
								mapDom.replaceChild(image, mapDom.firstChild)
								val imgwidth = image.offsetWidth
								val imgheight = image.offsetHeight
								val body = UploadSizeReq(imgwidth.toInt, imgheight.toInt).asJson.noSpaces
								Http.postJsonAndParse[CommonRsp](Routes.UserRoute.uploadSize, body).map {
									case Right(rsp) =>
										if(rsp.errCode == 0){
											Shortcut.alert(s"上传成功")
										} else {
											Shortcut.alert(s"上传失败: upload size error:${rsp.msg}")
										}
									case Left(_) =>
										Shortcut.alert(s"上传失败: upload size parse error")
								}
							} else {
								println(s"error:${info.msg}")
								Shortcut.alert(s"上传失败！")
							}
						case Left(error) =>
							println(s"parse error:$error")
							Shortcut.alert(s"上传失败！parse error:$error")
					}
				}
			}
		}
		
	}
	
	override def render(): Div = {
		div(*.cls:= "container", *.id:= "theContainer")(
			div(*.cls:= "row")(
				div(
					basicInfoDom, getUserBasicInfo,
					tableDom, getBoxList,
					label("地图上传"),
					fileUpload
				),
				div(*.overflowX:= "auto", *.marginBottom:= "80px")(
					mapDom
				)
			)
		).render
	}
}
