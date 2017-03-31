package wifiIntensity.models.dao

import wifiIntensity.models.tables.SlickTables
import wifiIntensity.utils.DBUtil._
import slick.driver.PostgresDriver.api._

/**
	* Created by 流風幻葬 on 2017/3/31.
	*/
object BoxDAO {
	private[this] val Box = SlickTables.tBoxs
	
	def getAllBoxs = db.run(
		Box.result
	)
	
}
