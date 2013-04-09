package org.isochrone

import org.isochrone.dbgraph.DatabaseGraph
import org.isochrone.dbgraph.GraphTables
import scala.slick.driver.PostgresDriver.simple._
import org.isochrone.dboutput.partition._
import org.isochrone.partition.merging._

trait Partitioner {
	self:ActionExecutor =>
	registerAction("partition", doPart)
	
	object notifier {
		var counter = 0
		def notify(size:Int) {
			if(counter==0)
				println(s"current partition size: $size")
			counter = (counter + 1) % 1000
		}
	}
	
	def doPart(args:IndexedSeq[String]) {
		if(args.size!=1) {
			println("usage: partition database")
		}
		val dbname = args(0)
		val db = Database.forURL("jdbc:postgresql:%s".format(dbname), driver="org.postgresql.Driver")
		db.withTransaction {
			implicit session:Session =>
			val graph = new DatabaseGraph(new GraphTables("road_nodes", "road_net_undir"), 200)
			implicit val gl = graph.graphlib
			val out = new DatabaseOutput("part_out")
			println("clearing output table")
			out.clear()
			println("loading nodes")
			val nodes = graph.allNodes
			println("computing partition")
			val part = org.isochrone.partition.merging.partition(nodes, 
					FunctionLibrary.mergePriority[Long] _, 
					FunctionLibrary.negAvgSearchGraphSize[Long] _,
					notifier.notify _)
			println("writing output")
			val toDb = for{
				(p, i) <- part.zipWithIndex
				n <- p
			} out.insert(n, i)
			println("done")
		}
	}
}