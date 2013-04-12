package org.isochrone.dijkstra

import org.scalatest.FunSuite
import org.isochrone.simplegraph.SimpleGraph
import org.isochrone.graphlib._
import org.isochrone.util.DoublePrecision
import org.isochrone.util.RandomGraph

class DijkstraIsochroneTest extends FunSuite {
	test("dijkstra isochrone finds an isochrone in star") {
		val edges = (1 to 10).map((0, _, 0.5)) ++ (1 to 10).map(x=>(x, x+10, 0.5))
		val graph = SimpleGraph(edges:_*)
		implicit val gl = graph.instance
		implicit val precision = DoublePrecision(0.01)
		val iso = DijkstraHelpers.isochrone(0, 0.6).toList
		assert(iso.map(_._1).toSet==(0 to 10).toSet)
	}
	
	test("dijkstra finds an isochrone on random graph") {
		for(i<- 1 to 3) {
			val g = RandomGraph.randomGraph(100, 300)
			implicit val gl = g.instance
			DijkstraHelpers.isochrone(1, 20.0)
		}
	}
}
