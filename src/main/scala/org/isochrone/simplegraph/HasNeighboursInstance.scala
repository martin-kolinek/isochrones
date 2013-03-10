package org.isochrone.simplegraph

import org.isochrone.graphlib.HasNeighbours

class HasNeighboursInstance(g: SimpleGraph) {
	implicit val simpleNodeHasNeighbours = new HasNeighbours[Int] {
		def neighbours(nd:Int) = g.getNeighbours(nd)
	}
}