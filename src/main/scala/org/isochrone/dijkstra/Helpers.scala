package org.isochrone.dijkstra

import org.isochrone.graphlib._

object DijkstraHelpers {
	def isochrone[T:HasNeighbours](start:T, max:Double) = 
		DijkstraAlgorithm.isochrone(Traversable(start->0.0), max)
}
