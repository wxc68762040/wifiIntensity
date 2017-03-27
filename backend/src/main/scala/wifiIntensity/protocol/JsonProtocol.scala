package wifiIntensity.protocol

/**
  * Created by ZYQ on 2016/6/1.
  **/

trait JsonProtocol

sealed trait RestResponse

case class SuccessRsp(
                       msg: String = "ok",
                       errCode: Int = 0
                     ) extends RestResponse

case class SuccessRspWithData(
                               data: String,
                               groupId: String = "-1",
                               msg: String = "ok",
                               errCode: Int = 0
                             ) extends RestResponse

case class RequestWithData(
                            appId: String,
                            nonce: String,
                            timestamp: String,
                            sn: String,
                            signature: String,
                            data: String
                          )

case class RequestWithoutData(
  appId: String,
  nonce: String,
  timestamp: String,
  sn: String,
  signature: String
)

final case class ErrorRsp(errCode: Int, msg: String) extends RestResponse
final case class JsonRsp(errCode: Int, msg: String) extends RestResponse