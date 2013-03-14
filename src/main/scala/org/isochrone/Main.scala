package org.isochrone

import org.isochrone.dboutput.DatabaseOutput
import scala.slick.driver.BasicDriver.simple._
import org.isochrone.dijkstra.DijkstraIsochrone._
import org.isochrone.dbgraph.DatabaseGraph
import org.isochrone.dbgraph.GraphTables
import graphlib._
import org.isochrone.util.DoublePrecision
import org.isochrone.util.Timing._

object Main {

	implicit val prec = DoublePrecision(0.00000001)
	
	def main(args: Array[String]): Unit = {
		if(args.size!=3 && args.size != 4) {
			println("usage: isochrones start_id cost_limit out_table")
			sys.exit(1)
		}
		val startNode = args(0).toLong
		val costLimit = args(1).toDouble
		val dbname = args(2)
		val onlyPrint = args.size==4
		val out = new DatabaseOutput("output")
		
		val db = Database.forURL("jdbc:postgresql:%s".format(dbname), driver="org.postgresql.Driver")
		db.withTransaction{ implicit session:Session =>
			println("Creating output table")
			if(!onlyPrint)
				out.create()
		    implicit val graph = new DatabaseGraph(new GraphTables("road_nodes", "road_net"), 200).graphlib
		    val isochrone = startNode.isochrone(costLimit)
		    println("Starting computation")
		    val time = timed{
				for((node, dist)<-isochrone) {
					if(!onlyPrint)
						out.insert(node, dist)
					println("%d is in isochrone with distance %f".format(node, dist))
				}
			}
		    println("Finished, took %dms".format(time))
		}
	}

}
