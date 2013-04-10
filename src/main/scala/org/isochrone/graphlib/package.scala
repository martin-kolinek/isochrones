package org.isochrone

import scala.language.implicitConversions

package object graphlib {
	implicit def toWithNeighbours[T:HasNeighbours](t:T) = new WithNeighbours(t)
	implicit class GraphOps[T](t:T) {
		def instance[Node](implicit ev:IsGraph[T, Node]) = new HasNeighbours[Node] {
			def neighbours(n:Node) = ev.neighbours(t, n)
		}
	}
	
	implicit class RegionGraphOps[T](t:T) {
		def nodeRegion[Node, Region](n:Node)(implicit ev:IsGraphWithRegions[T, Node, Region]) =
			ev.nodeRegion(t, n)
	}
	
	implicit def singleRegionGraphIsGraph[T, Node, Region] = new IsGraph[SingleRegionGraph[T, Node, Region], Node] {
		def neighbours(g:SingleRegionGraph[T, Node, Region], nd:Node) = g.getNeighbours(nd) 
	}
}
