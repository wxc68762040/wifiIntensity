package wifiIntensity.protocol

import wifiIntensity.ptcl.CommonRsp

/**
  * Created by ZYQ on 2016/12/13.
  **/
object ErrorCode {

  def internalError(message: String) = CommonRsp(s"internal error:$message", 1000100)
  val jsonFormatError = CommonRsp("json parse error.", 1000101)
  val signatureError = CommonRsp("signature wrong.",1000102)
  def requestIllegal(body: String = "") = CommonRsp(s"receive illegal request body:$body.", 1000103)
  val operationTimeOut = CommonRsp("operation time out.", 1000104)
  val appIdInvalid = CommonRsp("appId invalid.", 1000105)
  val notLogin = CommonRsp("user have not login", 1000205)
  
  val createUserError = CommonRsp("create user failed", 1000201)
  val userExisted = CommonRsp("user existed", 1000202)
  val wrongPassword = CommonRsp("password wrong", 1000203)
  val userNotExist = CommonRsp("user is not exist", 1000204)
  val addBoxError = CommonRsp("add box error", 1000205)
  val deleteBoxError = CommonRsp("delete box error", 1000206)
  val uploadFileError = CommonRsp("upload map error", 1000207)
  val uploadSizeError = CommonRsp("upload map size error", 1000208)
}