package wifiIntensity.utils

import java.io._

import akka.http.scaladsl.model.ContentType
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import wifiIntensity.common.AppSettings

/**
	* User: zhaorui
	* Date: 2016/4/23
	* Time: 20:49
	*/
object FileUtil {
	val log = LoggerFactory.getLogger(this.getClass)
	
	def storeTmpFile(tmpFile: File, extFileName: String) = {
		val is = new FileInputStream(tmpFile)
		val md5 = DigestUtils.md5Hex(is)
		is.close()
		val fileName = md5 + "." + extFileName
		val dest = getDestFile(fileName)
		log.info(s"file uploaded to: ${
			dest.getAbsolutePath
		}")
		if (!dest.getParentFile.exists()) {
			dest.getParentFile.mkdirs()
		}
		if (dest.exists()) {
			dest.delete()
		}
		copyFile(tmpFile, dest)
		if (tmpFile.exists()) {
			tmpFile.delete()
		}
		dest
	}
	
	def copyFile(srcFile: File, destFile: File) = {
		var byteRead = 0
		var in: InputStream = null
		var out: OutputStream = null
		try {
			in = new FileInputStream(srcFile)
			out = new FileOutputStream(destFile)
			val buffer = new Array[Byte](1024)
			
			byteRead = in.read(buffer)
			while (byteRead != -1) {
				out.write(buffer, 0, byteRead)
				byteRead = in.read(buffer)
			}
		} catch {
			case e: Exception =>
				log.error(s"Copy file failed, error: ${e.getMessage}")
		} finally {
			if (out != null)
				out.close()
			if (in != null)
				in.close()
		}
	}
	
	
	def getDestFile(fileName: String) = {
		val filePath = AppSettings.dataPath + "/"  + PathHasher.hashFilePath(fileName)
		new File(filePath).getAbsoluteFile
	}
	
	
	def getCacheFile(appId: String, fileName: String, w: Int, h: Int) = {
		val filePath = getCacheDir(appId) + cacheImageName(fileName, w, h)
		new File(filePath).getAbsoluteFile
	}
	
	def getCacheDir(appId: String) = {
		AppSettings.dataPath + "/" + appId + "/cache"
	}
	
	def getExtName(fileName: String) = {
		val extIndex = fileName.lastIndexOf('.')
		if (extIndex <= 0) None else Some(fileName.substring(extIndex + 1))
	}
	
	def imageContentType(fileName: String): Option[ContentType] = {
		getExtName(fileName) match {
			case Some(extName) => ContentType.parse(s"image/$extName") match {
				case Right(ct) => Some(ct)
				case _ => None
			}
			case None => None
		}
	}
	
	def splitExtName(fileName: String) = {
		val extIndex = fileName.lastIndexOf('.')
		if (extIndex <= 0) {
			(fileName, "")
		} else {
			(fileName.substring(0, extIndex), fileName.substring(extIndex + 1))
		}
	}
	
	def cacheImageName(fileName: String, w: Int, h: Int) = {
		val (f1, f2) = splitExtName(fileName)
		s"${
			f1
		}_w${
			w
		}_h${
			h
		}.$f2"
	}
	
}