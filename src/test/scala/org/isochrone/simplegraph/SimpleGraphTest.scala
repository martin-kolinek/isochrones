package org.isochrone.simplegraph

import org.scalatest.FunSuite
import org.isochrone.dijkstra.DefaultDijkstraProvider

class SimpleGraphTest extends FunSuite {
    test("SimpleGraph works") {
        new SimpleGraphComponent with DefaultDijkstraProvider {
            type NodeType = Int
            val graph = SimpleGraph(
                (1, 2, 0.1),
                (2, 3, 0.2),
                (2, 4, 0.3),
                (5, 2, 0.4),
                (5, 3, 0.5),
                (4, 5, 0.6),
                (3, 5, 0.7))
            val neigh = graph.neighbours(2)
            assert(neigh.toSet == Set((3, 0.2), (4, 0.3)))
            val neigh2 = graph.neighbours(5)
            assert(neigh2.toSet == Set((2, 0.4), (3, 0.5)))
        }
    }

    test("SimpleGraph returns empty list for nonexistent node") {
        new SimpleGraphComponent with DefaultDijkstraProvider {
            type NodeType = Int
            val graph = SimpleGraph(
                (1, 2, 0.1),
                (2, 3, 0.2),
                (2, 4, 0.3),
                (5, 2, 0.4),
                (5, 3, 0.5),
                (4, 5, 0.6),
                (3, 5, 0.7))
            val neigh = graph.neighbours(10)
            assert(neigh.size == 0)
        }
    }

    test("SimpleGraph works with regions") {
        new SimpleGraphComponent with DefaultDijkstraProvider {
            type NodeType = Int
            val dir = Seq(
                0 -> 1,
                1 -> 2,
                2 -> 3,
                3 -> 1,
                3 -> 4,
                4 -> 5,
                5 -> 6,
                6 -> 4)
            val undir = dir ++ dir.map(_.swap)
            val graph = SimpleGraph(undir.map(x => (x._1, x._2, 1.0)),
                Map(0 -> 0, 1 -> 0, 2 -> 0, 3 -> 0, 4 -> 1, 5 -> 1, 6 -> 1))
            assert(graph.nodeRegion(0).map(_.num) == Some(0))
            assert(graph.nodeRegion(5).map(_.num) == Some(1))
            assert(math.abs(graph.nodeEccentricity(6) - 1.0) < 0.0001)
            assert(math.abs(graph.nodeEccentricity(4) - 1.0) < 0.0001)
            assert(math.abs(graph.nodeEccentricity(1) - 1.0) < 0.0001)
            assert(math.abs(graph.nodeEccentricity(3) - 2.0) < 0.0001)
        }
    }

    test("SimpleGraph simpleRegion works") {
        new SimpleGraphComponent with DefaultDijkstraProvider {
            type NodeType = Int
            val graph = SimpleGraph(Seq((0, 1, 1.0), (1, 2, 1.0), (2, 0, 1.0)), Map(0 -> 1, 1 -> 1, 2 -> 2))
            val sing = graph.singleRegion(graph.nodeRegion(0).get)
            assert(sing.nodes.toSet == Set(0, 1))
            assert(sing.neighbours(1).size == 0)
            assert(sing.neighbours(0).size == 1)
        }
    }
}
