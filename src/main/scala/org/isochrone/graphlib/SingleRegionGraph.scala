package org.isochrone.graphlib

class SingleRegionGraph[T, Node, Region](val underlying:T, val region:Region)(implicit ev:IsGraphWithRegions[T, Node, Region]) {
    
	def getNeighbours(nd:Node) = {
		implicit val ins = underlying.instance
		nd.neighbours.filter(x=>underlying.nodeRegion(x._1)==Some(region))
	}
}
