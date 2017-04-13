package wifiIntensity.models.dao

import wifiIntensity.models.tables.{SlickTables, rBasicShoot}
import wifiIntensity.utils.DBUtil._
import slick.driver.PostgresDriver.api._

/**
	* Created by 流風幻葬 on 2017/3/30.
	*/
object BasicShootDAO {
	private[this] val BasicShoot = SlickTables.tBasicShoot
	
	def addShoots(shoots: List[rBasicShoot]) = db.run(
		(BasicShoot ++= shoots).asTry
	)
	
	def getShootsByTime(startTime: Long, endTime: Long) = db.run(
		BasicShoot.filter(e => e.timestamp < endTime && e.timestamp > startTime).result
	)
	
}
