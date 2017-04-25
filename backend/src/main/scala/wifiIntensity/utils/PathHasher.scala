package wifiIntensity.utils

import org.slf4j.LoggerFactory

/**
  * User: Taoz
  * Date: 3/5/2016
  * Time: 9:36 PM
  */
object PathHasher {

  val log = LoggerFactory.getLogger(this.getClass)

  val PATH_1_SIZE = 200
  val PATH_2_SIZE = 200

  private def hashCodes1(string: String, hashNum: Int) = {
    require(string.length>hashNum, s"string length[${string.length}] must larger than the number[$hashNum].")
    val unit = string.length / hashNum
    val (s1, s2) = string.splitAt(unit)
    List(s1.hashCode, s2.hashCode)
  }
  private def hashCodes(string: String, hashNum: Int) = {
    require(string.length>hashNum, s"string length[${string.length}] must larger than the number[$hashNum].")
    val unit = string.length / hashNum
    var current = string
    var lastCode = -1
    (0 until hashNum).map{ _ =>
      val newCode = (current + lastCode).hashCode
      val (s1, s2) = current.splitAt(unit)
      current = s2 + s1
      lastCode = newCode
      newCode
    }
  }

  def hashFilePath(fileName: String) = {
//    val codes = hashCodes(fileName, 2)
    //    val p1 = f"${Math.abs(codes(0)) % PATH_1_SIZE}%03d"
    //    val p2 = f"${Math.abs(codes(1)) % PATH_2_SIZE}%03d"
    //    val sb = new StringBuilder()
    //    sb.append(p1).append("/").append(p2).append("/").append(fileName).toString()
    val sb = new StringBuilder()
    sb.append(fileName).toString()
  }

  def main1(args: Array[String]) {
    //val fileName = SecureUtil.nonceStr(32) + ".jpg"


    (1 to 10).foreach{ _ =>
      val ss = SecureUtil.nonceStr(16)
      println(ss)

    }


  }

  def main(args: Array[String]) {

    val size1 = 20
    val size2 = 20


    val map = new collection.mutable.HashMap[(Int, Int), Int]()

    val t1 = System.currentTimeMillis()

    var c = 0
    (0 to 1000000).foreach{ _ =>
      val str = SecureUtil.nonceStr(32)
      val codes = hashCodes1(str, 2)
      val i1 = Math.abs(codes(0)) % 20
      val i2 = Math.abs(codes(1)) % 20
      map((i1,i2)) = map.getOrElseUpdate((i1, i2), 0) + 1

      if(codes(0) <= Int.MinValue) {
        c += 1
      }
    }

    val t2 = System.currentTimeMillis()

    val rst = map.toList.sortBy(_._1).mkString("\n")
    println(rst)
    println(f"${t2 - t1}%04d")
    println(c)




  }



}
