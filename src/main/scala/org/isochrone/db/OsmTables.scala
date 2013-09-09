package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry

trait OsmTableComponent {
    class Ways extends Table[(Long, Map[String, String], Geometry)]("ways") {
		def id = column[Long]("id")
		def tags = column[Map[String, String]]("tags")
		def linestring = column[Geometry]("linestring")
		def * = id ~ tags ~ linestring
	}
	
	class WayNodes extends Table[(Long, Long, Int)]("way_nodes") {
	    def wayId = column[Long]("way_id")
	    def nodeId = column[Long]("node_id")
	    def sequenceId = column[Int]("sequence_id")
	    def * = wayId ~ nodeId ~ sequenceId
	}
	
	class Nodes extends Table[(Long, Geometry)]("nodes") {
	    def id = column[Long]("id")
	    def geom = column[Geometry]("geom")
	    def * = id ~ geom
	}
	
	trait OsmTables {
	    val ways:Ways
	    val wayNodes:WayNodes
	    val nodes:Nodes
	}
	
	val osmTables:OsmTables

}