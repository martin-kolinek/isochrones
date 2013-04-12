package org.isochrone.graphlib

trait IsGraph[T, Node] {
	def neighbours(t:T, n:Node):Traversable[(Node, Double)]
}

trait IsGraphWithRegions[T, Node, Region] extends IsGraph[T, Node] {
	def nodeRegion(t:T, n:Node):Option[Region]
    def nodeExcentricity(n:Node):Double
}
