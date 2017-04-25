package wifiIntensity.models.dao

import wifiIntensity.models.tables.{SlickTables, rBoxs}
import wifiIntensity.utils.DBUtil._
import slick.driver.PostgresDriver.api._

/**
	* Created by 流風幻葬 on 2017/3/31.
	*/
object  BoxDAO {
	private[this] val Box = SlickTables.tBoxs
	
	def getAllBoxs = db.run(
		Box.result
	)
	
	def getBoxsByOwner(owner: Long) = db.run(
		Box.filter(_.owner === owner).result
	)
	
	def addBox(r: rBoxs) = db.run(
		(Box += r).asTry
	)
	
	def deleteBox(boxMac: String) = db.run(
		Box.filter(_.boxMac === boxMac).delete.asTry
	)
	
	def alterBox(boxMac: String, boxName: String, x: Double ,y: Double) = db.run(
		Box.filter(_.boxMac === boxMac).map(e => (e.boxName, e.x, e.y)).update((boxName, x, y)).asTry
	)
	
}
