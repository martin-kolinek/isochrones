package org.isochrone.dijkstra

import org.scalatest.FunSuite
import org.isochrone.simplegraph.SimpleGraph
import org.isochrone.graphlib._
import org.isochrone.dijkstra.DijkstraIsochrone._
import org.isochrone.util.DoublePrecision

class DijkstraIsochroneTest extends FunSuite {
	test("dijkstra isochrone finds an isochrone in star") {
		val edges = (1 to 10).map((0, _, 0.5)) ++ (1 to 10).map(x=>(x, x+10, 0.5))
		val graph = new SimpleGraph(edges:_*)
		implicit val gl = graph.graphlib
		implicit val precision = DoublePrecision(0.01)
		val iso = 0.isochrone(0.6).toList
		assert(iso.toSet==(0 to 10).toSet)
	}
}