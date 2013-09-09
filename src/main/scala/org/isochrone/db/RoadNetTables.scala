package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._

trait RoadNetTableComponent {
	class EdgeTable(name:String) extends Table[(Long, Long, Float)](name) {
		def start = column[Long]("start_node")
		def end = column[Long]("end_node")
		def cost = column[Float]("cost")
		def * = start ~ end ~ cost
	}

	class NodeTable(name:String) extends Table[(Long, Int)](name) {
		def id = column[Long]("id")
		def region = column[Int]("region")
		def * = id ~ region
	}

	class RegionTable(name:String) extends Table[(Int, Double)](name) {
		def id = column[Int]("id")
		def diameter = column[Double]("diameter")
		def * = id ~ diameter
	}
	
	trait RoadNetTables {
	    val roadNet:EdgeTable
	    val roadNetUndir:EdgeTable
	    val roadNodes:NodeTable
	}
	
	val roadNetTables:RoadNetTables
}