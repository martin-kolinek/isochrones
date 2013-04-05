package org.isochrone.dboutput.isochrone

import scala.slick.driver.PostgresDriver.simple._

class OutputTable(name:String) extends Table[(Long, Double)](name) {
	def node = column[Long]("node")
	def distance = column[Double]("distance")
	def * = node ~ distance
}
