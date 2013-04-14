package org.isochrone.dboutput.partition

import org.isochrone.graphlib._
import resource._
import org.isochrone.dbgraph.DatabaseGraphWithRegions
import scala.slick.driver.PostgresDriver.simple._
import org.isochrone.util._
import org.isochrone.partition.RegionAnalyzer
import org.isochrone.dbgraph.NodeTable
import org.isochrone.dbgraph.EdgeTable
import org.isochrone.dbgraph.RegionTable
import org.isochrone.dijkstra.DijkstraAlgorithm
import scala.collection.mutable.ListBuffer

class HigherLevelGraph(nodeTable:NodeTable, edgeTable:EdgeTable, regionTable:RegionTable)(implicit session:Session) {
	def createHigherLevelGraph(g:DatabaseGraphWithRegions, notification:Int=>Unit = x=>Unit) {
		val difRegs = for {
			n1 <- g.g.tables.nodes
			n2 <- g.g.tables.nodes if n1.region =!= n2.region
			e <- g.g.tables.edges if e.start === n1.id && e.end === n2.id
		} yield (n1, n2, e)
		edgeTable.insert(difRegs.map(_._3))
		for(it<-managed(difRegs.sortBy(_._1.region).map(x=>x._1.region -> x._1.id).elements)) {
			val regions = it.partitionBy(_._1)
			for (regionNodes <- regions) {
				val reg = regionNodes.head._1
				notification(reg)
				processRegion(g, reg, regionNodes.map(_._2))
			}
		}
	}
	
	def processRegion(graph:DatabaseGraphWithRegions, region:Int, nodes:Seq[Long]) {
		val single = new SingleRegionGraph(graph, region)
		var diam = 0.0
		for((node, others) <- RegionAnalyzer.borderNodeDistances(single, nodes.toSet)) {
			nodeTable.insert(node, 0)
			for((other, dist) <- others) {
				edgeTable.insert(node, other, dist)
			}
			if(!others.isEmpty)
				diam = math.max(diam, others.map(_._2).max)
		}
		regionTable.insert(region, diam)
	}
}