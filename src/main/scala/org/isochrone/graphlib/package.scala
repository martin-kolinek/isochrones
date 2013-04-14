package org.isochrone

import scala.language.implicitConversions

package object graphlib {
	implicit def toWithNeighbours[T:HasNeighbours](t:T) = new WithNeighbours(t)
	implicit class GraphOps[T](t:T) {
		def instance[Node](implicit ev:IsGraph[T, Node]) = new HasNeighbours[Node] {
			def neighbours(n:Node) = ev.neighbours(t, n)
		}
		def nodes[Node](implicit ev:IsGraph[T, Node]) = ev.nodes(t)
	}
	
	implicit class RegionGraphOps[T](t:T) {
		def nodeRegion[Node, Region](n:Node)(implicit ev:HasRegions[T, Node, Region]) =
			ev.nodeRegion(t, n)
        def eccentricity[Node, Region](n:Node)(implicit ev:HasRegions[T, Node, Region]) = ev.nodeEccentricity(t, n)
	}
	
    implicit def graphIsGraph[Node] = new IsGraph[Graph[Node], Node] {
        def neighbours(g:Graph[Node], n:Node) = g.neighbours(n)
        def nodes(g:Graph[Node]) = g.nodes
    }

    implicit def graphWithRegionsIsGraphWithRegions[Node, Region] = new HasRegions[GraphWithRegions[Node, Region], Node, Region] {
        def nodeRegion(g:GraphWithRegions[Node, Region], n:Node) = g.nodeRegion(n)
        def nodeEccentricity(g:GraphWithRegions[Node, Region], n:Node) = g.nodeEccentricity(n)
    }
    
    implicit def singleRegionGraphIsGraph[T, Node, Region](implicit ev:IsGraph[T, Node]) = new IsGraph[SingleRegionGraph[T, Node, Region], Node] {
		def neighbours(g:SingleRegionGraph[T, Node, Region], nd:Node) = g.getNeighbours(nd)
		def nodes(g:SingleRegionGraph[T, Node, Region]) = ev.nodes(g.underlying)
	}
}
