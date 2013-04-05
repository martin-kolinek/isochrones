package org.isochrone.dboutput.partition

import scala.slick.driver.PostgresDriver.simple._

class OutputTable(name:String) extends Table[(Long, Int)](name) {
	def nodeId = column[Long]("node_id")
	def region = column[Int]("region")
	def * = nodeId ~ region
}