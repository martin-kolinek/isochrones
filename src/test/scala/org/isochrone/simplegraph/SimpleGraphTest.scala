package org.isochrone.simplegraph

import org.scalatest.FunSuite
import org.isochrone.dijkstra.DefaultDijkstraProvider

class SimpleGraphTest extends FunSuite {
    test("SimpleGraph works") {
        new SimpleGraphComponent with DefaultDijkstraProvider {
            val sg = SimpleGraph(
                (1, 2, 0.1),
                (2, 3, 0.2),
                (2, 4, 0.3),
                (5, 2, 0.4),
                (5, 3, 0.5),
                (4, 5, 0.6),
                (3, 5, 0.7))
            val neigh = sg.neighbours(2)
            assert(neigh.toSet == Set((3, 0.2), (4, 0.3)))
            val neigh2 = sg.neighbours(5)
            assert(neigh2.toSet == Set((2, 0.4), (3, 0.5)))
        }
    }

    test("SimpleGraph returns empty list for nonexistent node") {
        new SimpleGraphComponent with DefaultDijkstraProvider {
            val sg = SimpleGraph(
                (1, 2, 0.1),
                (2, 3, 0.2),
                (2, 4, 0.3),
                (5, 2, 0.4),
                (5, 3, 0.5),
                (4, 5, 0.6),
                (3, 5, 0.7))
            val neigh = sg.neighbours(10)
            assert(neigh.size == 0)
        }
    }

    test("SimpleGraph works with regions") {
        new SimpleGraphComponent with DefaultDijkstraProvider {
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
            val sg = SimpleGraph(undir.map(x => (x._1, x._2, 1.0)),
                Map(0 -> 0, 1 -> 0, 2 -> 0, 3 -> 0, 4 -> 1, 5 -> 1, 6 -> 1))
            assert(sg.nodeRegion(0) == Some(0))
            assert(sg.nodeRegion(5) == Some(1))
            assert(math.abs(sg.nodeEccentricity(6) - 1.0) < 0.0001)
            assert(math.abs(sg.nodeEccentricity(4) - 1.0) < 0.0001)
            assert(math.abs(sg.nodeEccentricity(1) - 1.0) < 0.0001)
            assert(math.abs(sg.nodeEccentricity(3) - 2.0) < 0.0001)
        }
    }

}
