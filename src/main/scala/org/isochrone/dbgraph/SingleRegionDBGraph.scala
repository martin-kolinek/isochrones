package org.isochrone.dbgraph

import org.isochrone.graphlib._

class SingleRegionDBGraph(val underlying:DatabaseGraph, val region:DatabaseGraph#Region)  {
    
    def getNeighbours(nd:underlying.Node) = {
        underlying.getNeighbours(nd).filter(x=>underlying.nodeRegion(x._1)==Some(region))
    }

    def graphlib = new HasNeighbours[Long] {
		def neighbours(n:Long) = getNeighbours(n)
	}
}
