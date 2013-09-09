package org.isochrone.dboutput.partition

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.dbgraph.NodeTable

class DatabaseOutput(name:String) {
	val tbl = new NodeTable(name)
	def create()(implicit session:Session) {tbl.ddl.create}
	def insert(node:Long, region:Int)(implicit session:Session) {
		tbl.insert((node, region))
	}
    def clear()(implicit session:Session) {
        tbl.map(identity).delete
    }
}