package wifiIntensity.utils

import org.apache.commons.math3.stat.regression.SimpleRegression

/**
	* Created by 流風幻葬 on 2017/4/14.
	*/
object Test{
	
	def main(args: Array[String]) = {
		val reg = new SimpleRegression(true)
		reg.addData(1,1)
		reg.addData(2,2)
		reg.addData(2,1)
		printf(s"x= ${reg.getIntercept}, y=${reg.getSlope}")
	}
	
}
