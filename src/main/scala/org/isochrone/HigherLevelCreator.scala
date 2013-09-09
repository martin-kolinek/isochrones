package org.isochrone

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.dbgraph.DatabaseGraph
import org.isochrone.dbgraph.GraphTables
import org.isochrone.dbgraph.DatabaseGraphWithRegions
import org.isochrone.dbgraph.RegionTable
import org.isochrone.dboutput.partition.HigherLevelGraph
import org.isochrone.dbgraph.NodeTable
import org.isochrone.dbgraph.EdgeTable

trait HigherLevelCreator {
	self:ActionExecutor =>
		
	registerAction("higher", higher)
	
	def higher(args:Seq[String]) {
		if(args.size != 1) {
			println("usage: higher database")
			sys.exit(1)
		}
		val dbname = args(0)
		val db = Database.forURL("jdbc:postgresql:%s".format(dbname), driver="org.postgresql.Driver")
		println("Connecting to database")
		db.withTransaction {
			implicit session:Session =>
			val nodeTable = new NodeTable("higher_nodes")
			val regionTable = new RegionTable("road_regions")
			val edgeTable = new EdgeTable("higher_edges")
			println("Clearing output tables")
			nodeTable.map(identity).delete
			edgeTable.map(identity).delete
			regionTable.map(identity).delete
			val graph = new DatabaseGraphWithRegions(
					new DatabaseGraph(new GraphTables("road_nodes", "road_net"), 200),
					new RegionTable("road_regions"))
			
			val hg = new HigherLevelGraph(nodeTable, edgeTable, regionTable)
			
			hg.createHigherLevelGraph(graph, x=>println(s"processing region $x"))
		}
		println("Done")
	}
}