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
		val onlyComp = args.size==4
		val out = new DatabaseOutput("output")
		
		val db = Database.forURL("jdbc:postgresql:%s".format(dbname), driver="org.postgresql.Driver")
		db.withTransaction{ implicit session:Session =>
			println("Clearing output table")
			if(!onlyComp)
				out.clear()
            var retrievals = 0
            val graph = new DatabaseGraph(new GraphTables("road_nodes", "road_net"), 200, {retrievals+=1})
            println("Preloading starting node")
            graph.ensureInMemory(startNode)
		    implicit val graphimp = graph.graphlib
		    val isochrone = startNode.isochrone(costLimit)
		    println("Starting computation")
		    val time = timed{
                var i = 0
				for((node, dist)<-isochrone) {
					if(!onlyComp) {
						out.insert(node, dist)
                    }
                    i+=1
                    if(i%1000==0)
                        println("Currently at distance %f".format(dist))
				}
			}
		    println("Finished computation, took %dms with %d access(es) to database".format(time, retrievals))
            println("Commiting data")
		}
        println("Done")
	}

}
