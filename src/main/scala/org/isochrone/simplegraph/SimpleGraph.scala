package org.isochrone.simplegraph

class SimpleGraph(edges:(Int, Int, Double)*) {
	private val neighbours = edges.groupBy(_._1).map{case (k,v) => (k, v.map(x=>(x._2, x._3)))}
	
	def nodes = (neighbours.keys ++ neighbours.values.flatMap(identity).map(_._1)).toSet
	
	def getNeighbours(node:Int) = neighbours.getOrElse(node, Seq())
	
	def graphlib = new HasNeighboursInstance(this).simpleNodeHasNeighbours
	
	override def toString = s"SimpleGraph($edges)"
}