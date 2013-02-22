package org.isochrone.graphlib

trait IsGraph[T] {
    type Node
    def neigh(t:T):HasNeighbours[Node]
}

/*class WithIsGraphOps[T:IsGraph](t:T)(implicit imp:IsGraph[T]) {
    def neigh = imp.neigh(t)
}*/

trait IsGraphWithRegions[T] extends IsGraph[T] {
    type Region
    def nodeRegion(t:T, n:Node):Option[Region]
}

/*class WithIsGraphWithRegionsOps[T](t:T)(implicit imp:IsGraphWithRegions[T])  {
    def nodeRegion(n:imp.Node) = imp.nodeRegion(t, n)
}*/
