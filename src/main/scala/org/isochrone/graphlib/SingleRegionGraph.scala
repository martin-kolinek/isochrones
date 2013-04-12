package org.isochrone.graphlib

class SingleRegionGraph[T, Node, Region](val underlying:T, val region:Region)(implicit ev2:IsGraph[T, Node], ev:HasRegions[T, Node, Region]) {
    
	def getNeighbours(nd:Node) = {
		implicit val ins = underlying.instance
		nd.neighbours.filter(x=>underlying.nodeRegion(x._1)==Some(region))
	}
}
