package org.isochrone

import org.isochrone.dboutput.DatabaseOutput
import scala.slick.driver.BasicDriver.simple._

object Main {

	def main(args: Array[String]): Unit = {
		if(args.size!=3) {
			println("usage: isochrones start_id cost_limit out_table")
			sys.exit(1)
		}
		val startNode = args(0).toInt
		val costLimit = args(1).toDouble
		val outTable = args(2)
		val out = new DatabaseOutput(outTable)
		
		val db = Database.forURL("jdbc:postgresql:martin", driver="org.postgresql.Driver")
		db.withSession { implicit session:Session =>
		    out.create()
		    
		}
	}

}