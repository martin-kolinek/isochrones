package org.isochrone.dboutput

import scala.slick.driver.BasicDriver.simple._

class DatabaseOutput(name:String) {
	val tbl = new OutputTable(name)
	def create()(implicit session:Session) {tbl.ddl.create}
	def insert(node:Long, distance:Double)(implicit session:Session) {
		tbl.insert((node, distance))
	}
}