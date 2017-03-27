package wifiIntensity.utils

import java.io.{ByteArrayInputStream, DataInputStream}

import org.slf4j.LoggerFactory

/**
  * Created by liuziwei on 2016/6/22.
  */

case class Shoot(id:Long,boxMac:String,clientMac:String,t:Long,rssi:Array[Int],src:String){
  private val s1 = "#"
  private val s2 = ","

  override def toString: String = {
    val sb = new StringBuilder()
    sb ++= id.toString ++= s1
    sb ++= boxMac ++= s1
    sb ++= clientMac ++= s1
    sb ++= rssi(0).toString ++= s2 ++= rssi(1).toString ++= s2 ++= rssi(2).toString ++= s1
    sb ++= t.toString ++= s1
    sb ++= src
    sb.toString()
  }
}

object ShootUtil {

  private val log = LoggerFactory.getLogger(this.getClass)
  val p1 = "#".r
  val p2 = ",".r
  val p3 = ":".r

  def boxLine2Shoot(line:String,id:Long,boxMac:String) = {
    val infos = p1.split(line)
    if (infos.length == 4) {
      val clientMac = p3.replaceAllIn(infos(0), "").toUpperCase
      val t = infos(2).toLong * 1000
      val rssi = p2.split(infos(1)).map(_.toInt)
      val src = infos(3)
      if (rssi.length == 3) {
        val shoot = Shoot(id,boxMac, clientMac, t, rssi, src)
        log.info(s"data line $line to shoot ${shoot.toString}")
        Some(shoot)
      } else {
        log.warn(s"data format of AP [$boxMac] error, rssi should have 3 values: $line}")
        None
      }
    } else {
      log.warn(s"data format of AP [$boxMac] error, data line should have 4 parts: $line}")
      None
    }
  }

  def line2Shoot(line: String) = {
    val infos = p1.split(line)
    if (infos.length == 6) {
      val id = infos(0).toLong
      val boxMac = infos(1)
      val clientMac = infos(2)
      val rssi = p2.split(infos(3)).map(_.toInt)
      val t = infos(4).toLong
      val src = infos(5)
      if (rssi.length == 3) {
        Some(Shoot(id, boxMac, clientMac, t, rssi, src))
      } else {
        log.warn(s"file data format error, rssi should have 3 values: $line}")
        None
      }
    } else {
      log.warn(s"file data format error, data line should have 4 parts: $line}")
      None
    }
  }

  def parseBytesToLong(bytes: Array[Byte]): Long = {

    val binput = new ByteArrayInputStream(bytes)
    val dinput = new DataInputStream(binput)
    val i = dinput.readLong()
    dinput.close()
    binput.close()
    i
  }

  def parseBytesToInt(bytes: Array[Byte]): Int = {

    val binput = new ByteArrayInputStream(bytes)
    val dinput = new DataInputStream(binput)
    val i = dinput.readInt()
    dinput.close()
    binput.close()
    i
  }

  def formatMac(originMac: String) = {
    val  sb = new StringBuffer(originMac)
    var  index = 2
    while (sb.length() > 2 && index < sb.length() - 1) {
      sb.insert(index, ":")
      index += 2 + 1
    }
    sb.toString
  }


}
