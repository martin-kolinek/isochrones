package org.isochrone


import scala.slick.driver.PostgresDriver.simple._
import org.isochrone.dboutput.partition._
import org.isochrone.partition.merging._
import org.isochrone.graphlib._
/*
trait Partitioner {
	self:ActionExecutor =>
	registerAction("partition", doPart)
	
	object notifier {
		var counter = 0
		def notify(size:Int, value:Double) {
			if(counter==0 || size < 400)
				println(s"current partition size: $size with value $value")
			counter = (counter + 1) % 1000
		}
	}
	
	private def doPart(args:IndexedSeq[String]) {
		if(args.size!=1) {
			println("usage: partition database")
		}
		val dbname = args(0)
		val db = Database.forURL("jdbc:postgresql:%s".format(dbname), driver="org.postgresql.Driver")
		db.withTransaction {
			implicit session:Session =>
			val graph = new DatabaseGraph(new GraphTables("road_nodes", "road_net_undir"), 200)
			implicit val gl = graph.instance
			val out = new DatabaseOutput("road_nodes")
			println("loading nodes")
			val nodes = graph.nodes
			println("computing partition")
			val part = org.isochrone.partition.merging.partition(nodes, 
					FunctionLibrary.randMergePriority[Long] _, 
					FunctionLibrary.boundaryEdgesCellSize[Long](70),
					notifier.notify _)
			println("writing output")
			println("clearing output table")
			out.clear()
			val toDb = for{
				(p, i) <- part.zipWithIndex
				n <- p
			} out.insert(n, i)
			println("done")
		}
	}
}
*/