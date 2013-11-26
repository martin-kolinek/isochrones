package org.isochrone.areas

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponent
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider

class AreaCheckerTest extends FunSuite {
    test("AreaChecker identifies wrong area") {
        new AreaCheckerComponent with GraphComponent with SimpleGraphComponent with DefaultDijkstraProvider {
            type NodeType = Int
            val graph = SimpleGraph.undirNoCost(1 -> 2, 2 -> 3, 3 -> 4, 4 -> 1, 1 -> 3)
            val ar = Area(List(1, 2, 3, 4))
            assert(AreaChecker.areaHasDiagonals(ar))
        }
    }

    test("AreaChecker does not identify correct area") {
        new AreaCheckerComponent with GraphComponent with SimpleGraphComponent with DefaultDijkstraProvider {
            type NodeType = Int
            val graph = SimpleGraph.undirNoCost(1 -> 2, 2 -> 3, 3 -> 4, 4 -> 1, 1 -> 3)
            val ar = Area(List(1, 2, 3))
            assert(!AreaChecker.areaHasDiagonals(ar))
        }
    }
}