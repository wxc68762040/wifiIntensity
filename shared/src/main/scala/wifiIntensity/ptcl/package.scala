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
  
  case class CommonRsp(msg: String = "ok", errCode: Int = 0) extends Response
  
  case class RegisterReq(userName: String, password: String) extends Request
  case class LoginReq(userName: String, password: String) extends Request
}
