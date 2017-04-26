package wifiIntensity.models.dao

import wifiIntensity.common.Constants
import slick.driver.PostgresDriver.api._
import wifiIntensity.utils.DBUtil.db
import wifiIntensity.models.tables.SlickTables._
import wifiIntensity.utils.SecureUtil

/**
	* Created by 流風幻葬 on 2017/4/17.
	*/
object UserDAO {
	
	def getUser(userName: String) = db.run {
		tUsers.filter(_.userName === userName).result.headOption
	}
	
	def getUserByUid(uid: Long) = db.run {
		tUsers.filter(_.uid === uid).result.headOption
	}

	def createUser(name: String, password: String) = {
		val createTime = System.currentTimeMillis
		val encrypted = SecureUtil.getSecurePassword(password, "127.0.0.1", createTime)
		db.run(
			tUsers.map(e => (e.userName, e.password, e.createTime)).returning(tUsers.map(_.uid)) += ((name, encrypted, createTime))
		)
	}
	
	def deleteUser(uid: Long) = db.run {
		tUsers.filter(_.uid === uid).delete.asTry
	}

	def uploadMap(uid: Long, file: String) = db.run {
		tUsers.filter(_.uid === uid).map(_.file).update(Some(file)).asTry
	}
	
	def uploadSize(uid: Long, width: Int, height: Int) = db.run {
		tUsers.filter(_.uid === uid).map(e => (e.width, e.height)).update((Some(width), Some(height))).asTry
	}
}
