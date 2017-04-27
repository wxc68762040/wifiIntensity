package wifiIntensity.models.dao

import wifiIntensity.models.tables.{SlickTables, rClientLocation}
import wifiIntensity.utils.DBUtil._
import slick.driver.PostgresDriver.api._

/**
	* Created by 流風幻葬 on 2017/4/14.
	*/
object ClientLocationDAO {
	private[this] val ClientLocation = SlickTables.tClientLocation
	
	def addRecords(r: List[rClientLocation]) = db.run(
		(ClientLocation ++= r).asTry
	)
	
	def getRecordsByTime(start: Long, end: Long) = db.run(
		ClientLocation.filter(e => e.timestamp >= start && e.timestamp <= end).result
	)
}
