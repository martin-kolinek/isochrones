package org.isochrone.graphlib

trait IsGraph[-T, Node] {
	def neighbours(t:T, n:Node):Traversable[(Node, Double)]
	def nodes(t:T):Traversable[Node]
}

trait HasRegions[-T, Node, Region] {
	def nodeRegion(t:T, n:Node):Option[Region]
    def nodeEccentricity(t:T, n:Node):Double
}

trait Graph[Node] {
    def neighbours(n:Node):Traversable[(Node, Double)]
    def nodes:Traversable[Node]
}

trait GraphWithRegions[Node, Region] extends Graph[Node] {
    def nodeRegion(n:Node):Option[Region]
    def nodeEccentricity(n:Node):Double
}
