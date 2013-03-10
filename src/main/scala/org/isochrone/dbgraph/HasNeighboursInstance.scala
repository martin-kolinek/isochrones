package org.isochrone.dbgraph

import org.isochrone.graphlib.HasNeighbours

class HasNeighboursInstance(g:DatabaseGraph) {
	implicit val nodeHasNeighbours = new HasNeighbours[Long] {
		def neighbours(n:Long) = g.getNeighbours(n)
	}
}
