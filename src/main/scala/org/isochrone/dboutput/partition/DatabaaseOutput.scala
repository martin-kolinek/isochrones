package org.isochrone.dboutput.partition

import scala.slick.driver.PostgresDriver.simple._

class DatabaaseOutput(name:String) {
	val tbl = new OutputTable(name)
	def create()(implicit session:Session) {tbl.ddl.create}
	def insert(node:Long, region:Int)(implicit session:Session) {
		tbl.insert((node, region))
	}
    def clear()(implicit session:Session) {
        tbl.map(identity).delete
    }
}