package wifiIntensity.utils

import org.apache.commons.math3.stat.regression.SimpleRegression

/**
	* Created by 流風幻葬 on 2017/4/14.
	*/
object Test{
	
	def main(args: Array[String]) = {
		val reg = new SimpleRegression(true)
		reg.addData(3.0103,-52.42857)
		reg.addData(0,-52.21053)
		reg.addData(-3.0103,-52.55)
		reg.addData(-4.7712,-53.7222)
		reg.addData(-6.0206,-55.08696)
		reg.addData(-6.9897,-60.67742)
		printf(s"x= ${reg.getIntercept}, y=${reg.getSlope}")
	}
	
}
