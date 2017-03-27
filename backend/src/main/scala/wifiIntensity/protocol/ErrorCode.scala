package wifiIntensity.protocol

import com.neo.sk.nyx.ptcl.CommonRsp

/**
  * Created by ZYQ on 2016/12/13.
  **/
object ErrorCode {

  val jsonFormatError = CommonRsp("json parse error.", 1000101)
  val signatureError = CommonRsp("signature wrong.",1000102)
  def requestIllegal(body: String = "") = CommonRsp(s"receive illegal request body:$body.", 1000103)
  val operationTimeOut = CommonRsp("operation time out.", 1000104)
  val unknownReason = CommonRsp("unknown reasons.", 1000105)
  val responseIllegal = CommonRsp("response illegal.", 1000106)
  val appIdInvalid = CommonRsp("appId invalid.", 1000106)
  val groupsNotExist = CommonRsp("No group found.", 1000107)

  def internalError(message: String) = CommonRsp(s"internal  error:$message", 1000201)
  def externalError(message: String) = CommonRsp(s"external reason:$message", 1000202)
  
  def addSectionError(section: String) = CommonRsp(s"add $section error", 1000301)
  def deleteSectionError(section: String) = CommonRsp(s"delete $section error", 1000302)
  def editSectionError(section: String) = CommonRsp(s"edit $section error", 1000303)
  def editAffiliationBatchError(section: String) = CommonRsp(s"set $section affiliation error", 1000304)
  def affiliationExistedError(sections: List[String]) = CommonRsp(s"below sections' affiliation have been set: $sections", 1000305)
  val keyValueExistedError = CommonRsp("key value already existed", 1000306)
  def boxNotExistError(boxes: List[String]) = CommonRsp(s"boxes below is not exist: $boxes", 1000307)
  val editNameError = CommonRsp("edit name error", 1000308)
  val boxMacEmptyError = CommonRsp("box mac shouldn't be empty", 1000309)
}