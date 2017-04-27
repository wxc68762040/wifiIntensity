package wifiIntensity

/**
  * Created by 流風幻葬 on 2017/4/17.
  */
package object ptcl {

  sealed trait Request
  sealed trait Response{
    val msg: String
    val errCode:Int
  }
  
  case class RegisterReq(userName: String, password: String) extends Request
  case class LoginReq(userName: String, password: String) extends Request
  case class AddBoxReq(boxMac: String, boxName: String, x: Double, y: Double, verticalHeight: Double) extends Request
  case class DeleteBoxReq(boxMac: String) extends Request
  case class UploadSizeReq(width: Int, height: Int) extends Request
  case class HeatDataReq(start: Long, end: Long) extends Request
  
  case class BoxInfo(boxMac: String, boxName: String, x: Double, y: Double)
  case class HeatData(x: Int, y: Int, total: Int)
  
  case class CommonRsp(msg: String = "ok", errCode: Int = 0) extends Response
  case class SessionNameRsp(username: String, msg: String = "ok", errCode: Int = 0) extends Response
  case class UserInfoRsp(username: String, file: String, width: Int, height: Int, msg: String = "ok", errCode: Int = 0) extends Response
  case class BoxListRsp(boxList: List[BoxInfo], msg: String = "ok", errCode: Int = 0) extends Response
  case class UploadMapRsp(mapPath: String, msg: String = "ok", errCode: Int = 0) extends Response
  case class HeatDataRsp(dataList: List[HeatData], msg: String = "ok", errCode: Int = 0) extends Response
}
