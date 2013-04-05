package org.isochrone.dboutput.isochrone

import scala.slick.driver.PostgresDriver.simple._

class DatabaseOutput(name:String) {
	val tbl = new OutputTable(name)
	def create()(implicit session:Session) {tbl.ddl.create}
	def insert(node:Long, distance:Double)(implicit session:Session) {
		tbl.insert((node, distance))
	}
    def clear()(implicit session:Session) {
        tbl.map(identity).delete
    }
}
