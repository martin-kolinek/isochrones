package org.isochrone.simplegraph

import org.isochrone.graphlib._
import org.isochrone.dijkstra.DijkstraAlgorithm

class SimpleGraph private (edges:Seq[(Int, Int, Double)], nodeRegions:Map[Int, Int]) extends GraphWithRegions[Int, Int] {
	private val neigh = edges.groupBy(_._1).map{case (k,v) => (k, v.map(x=>(x._2, x._3)))}
	
	lazy val nodeEccentrities = {
		val x = for {
			node <- nodes.toSeq
			region <- nodeRegions.get(node)
			single = new SingleRegionGraph(this, nodeRegions(node))
		} yield node -> DijkstraAlgorithm.compute(Seq(node -> 0.0))(single.instance).toSeq.last._2
		x.toMap
	}
	
	def nodes = (neigh.keys ++ neigh.values.flatMap(identity).map(_._1)).toSet
	
	def neighbours(node:Int) = neigh.getOrElse(node, Seq())
	
	def nodeRegion(node:Int) = nodeRegions.get(node)
	
	def nodeEccentricity(n:Int) = nodeEccentrities(n)
	
	override def toString = s"SimpleGraph($edges)"
}

object SimpleGraph {
	def apply(edges:(Int, Int, Double)*) = new SimpleGraph(edges, (edges.map(_._1)++edges.map(_._2)).map(_->0).toMap)
	def apply(edges:Seq[(Int, Int, Double)], nodes:Map[Int, Int]) = new SimpleGraph(edges, nodes)
}
