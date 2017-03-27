package wifiIntensity.utils

import java.io.{File, FileFilter, PrintWriter}

import play.api.Logger
import wifiIntensity.common.AppSettings
import wifiIntensity.common.Constants.Behavior

import scala.io.Source

/**
  * Created by liuziwei on 2016/7/10.
  */
object FileUtil {
  private[this] val log = Logger(this.getClass)

  val targetDir = new File(AppSettings.dataPath)
  if(!targetDir.exists){
    targetDir.mkdirs()
  }

  def saveDuration(
    fileName: String,
    unitId:String,
    durationCache: Map[String, List[(Long, Long)]]
  ) = {
    try {
      val durationFile = new File(AppSettings.dataPath + s"$unitId/$fileName").getAbsoluteFile
      if (durationFile.exists()) durationFile.delete()
      val writer = new PrintWriter(durationFile)
      for (r <- durationCache) {
        val clientMac = r._1
        val durationList = r._2
        writer.write(s"$unitId,$clientMac#${durationList.map(t => t._1 + ":" + t._2).mkString(",")}")
        writer.write("\n")
      }
      writer.close()
    }catch{
      case e:Exception =>
        log.error(s"save file $unitId/$fileName error",e)
    }

    if(durationCache.nonEmpty ) log.debug(s"saveDuration: ${durationCache.size} lines written into $unitId/$fileName.")
  }

  def readDuration(unitId: String, fileName: String, behavior: String = "all", durationLength: Int = AppSettings.visitDurationLength) = {
    //behavior = in || all || out
    val durationFile = new File(AppSettings.dataPath + s"$unitId/" + fileName).getAbsoluteFile
    val cache = if(durationFile.exists()){
      val source = Source.fromFile(durationFile,"UTF-8")
      val cache = source.getLines().flatMap{ line =>
        val clientMac = line.split("#")(0).split(",").last
        val durationList = line.split("#")(1).split(",").flatMap{ s =>
          val (t1, t2) = (s.split(":")(0).toLong, s.split(":")(1).toLong)
          val duration = t2 - t1
          behavior match {
            case Behavior.in =>
              if (duration >= durationLength) Some((t1, t2)) else None
            case Behavior.out =>
              if (duration < durationLength) Some((t1, t2)) else None
            case _ =>
              Some((t1, t2))
          }
        }.toList
        if (durationList.nonEmpty) Some((clientMac, durationList)) else None
      }.toMap
      source.close()
      cache
    }else {
      Map[String,List[(Long,Long)]]()
    }
    cache
  }

  def deleteDuration(unitId: String, fileName: String): Boolean = {
    new File(AppSettings.dataPath + s"$unitId/$fileName").getAbsoluteFile.delete()
  }

  def listDurations(unitId: String, fileFilter: FileFilter): Array[File] = {
    new File(AppSettings.dataPath + s"$unitId").getAbsoluteFile.listFiles(fileFilter)
  }

}
