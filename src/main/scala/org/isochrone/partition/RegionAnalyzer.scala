package org.isochrone.partition
import org.isochrone.graphlib._
import org.isochrone.dijkstra.DijkstraAlgorithm

object RegionAnalyzer {
	def borderNodeDistances[T, Node](g:T, borderNodes:Set[Node])(implicit ev:IsGraph[T, Node]) = {
		implicit val gl = g.instance
		val res = for{
			n<-borderNodes
			dijk = DijkstraAlgorithm.compute(Seq(n->0.0))
		} yield n -> dijk.filter(x => borderNodes.contains(x._1) && x._1 != n)
		res.toSeq
	}
}