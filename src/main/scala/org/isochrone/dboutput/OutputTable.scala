package org.isochrone.dboutput

import scala.slick.driver.BasicDriver._

class OutputTable(name:String) extends Table[(Long, Int)](name) {
	def node = column[Long]("node")
	def region = column[Int]("region")
	def * = node ~ region
}
