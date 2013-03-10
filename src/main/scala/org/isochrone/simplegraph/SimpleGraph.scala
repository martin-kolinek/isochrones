package org.isochrone.simplegraph

class SimpleGraph(edges:(Int, Int, Double)*) {
	private val neighbours = edges.groupBy(_._1).map{case (k,v) => (k, v.map(x=>(x._2, x._3)))}
	def getNeighbours(node:Int) = neighbours(node)
	
	def graphlib = new HasNeighboursInstance(this)
}