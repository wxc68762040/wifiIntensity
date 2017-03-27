package wifiIntensity.utils

import org.apache.commons.codec.digest.DigestUtils
import wifiIntensity.protocol.RequestWithData

import scala.util.Random

/**
  * User: Taoz
  * Date: 7/8/2015
  * Time: 8:42 PM
  */
object SecureUtil {

  val random = new Random(System.currentTimeMillis())

  val chars = Array(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  )


  def getSecurePassword(password: String, ip: String, timestamp: Long): String = {
    DigestUtils.sha1Hex(DigestUtils.md5Hex(timestamp + password) + ip + timestamp)
  }

  def checkSignature(parameters: List[String], signature: String, secureKey: String) = {
    generateSignature(parameters, secureKey) == signature
  }

  def generateSignature(parameters: List[String], secureKey: String) = {
    val strSeq = ( secureKey :: parameters ).sorted.mkString("")
    DigestUtils.sha1Hex(strSeq)
  }

  def generateSignatureParameters(parameters: List[String], secureKey: String) = {
    val timestamp = System.currentTimeMillis().toString
    val nonce = nonceStr(6)
    val pList = nonce :: timestamp :: parameters
    val signature = generateSignature(pList, secureKey)
    (timestamp, nonce, signature)
  }

  def checkPostEnvelope(request: RequestWithData, secureKey: String) = {
    import request._
    val params = List(appId, sn, timestamp, nonce, data)
    checkSignature(params, signature, secureKey)
  }


  def genPostEnvelope(appId: String, sn: String, data: String, secureKey: String) = {
    val params = List(appId, sn, data)
    val (timestamp, nonce, signature) = generateSignatureParameters(params, secureKey)
    RequestWithData(appId, nonce, timestamp, sn, signature, data)
  }

  def nonceStr(length: Int) = {
    val range = chars.length
    (0 until length).map { _ =>
      chars(random.nextInt(range))
    }.mkString("")
  }




}