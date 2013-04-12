package org.isochrone.simplegraph

import org.isochrone.graphlib._

class SimpleGraph(edges:(Int, Int, Double)*) extends Graph[Int] {
	private val neigh = edges.groupBy(_._1).map{case (k,v) => (k, v.map(x=>(x._2, x._3)))}
	
	def nodes = (neigh.keys ++ neigh.values.flatMap(identity).map(_._1)).toSet
	
	def neighbours(node:Int) = neigh.getOrElse(node, Seq())
	
	override def toString = s"SimpleGraph($edges)"
}
