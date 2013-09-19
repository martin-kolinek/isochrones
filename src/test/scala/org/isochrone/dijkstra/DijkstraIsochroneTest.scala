package org.isochrone.dijkstra

import org.scalatest.FunSuite
import org.isochrone.graphlib._
import org.isochrone.util.DoublePrecision
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.util.RandomGraphComponent
import org.isochrone.util.RandomGraphComponent

class DijkstraIsochroneTest extends FunSuite {
    test("dijkstra isochrone finds an isochrone in star") {
        new SimpleGraphComponent with DefaultDijkstraProvider with DijkstraAlgorithmComponent {
            val edges = (1 to 10).map((0, _, 0.5)) ++ (1 to 10).map(x => (x, x + 10, 0.5))
            val graph = SimpleGraph(edges: _*)
            val iso = DijkstraHelpers.isochrone(0, 0.6).toList
            assert(iso.map(_._1).toSet == (0 to 10).toSet)
        }
    }

    test("dijkstra finds an isochrone on random graph") {
        for (i <- 1 to 3) {
            new RandomGraphComponent with DefaultDijkstraProvider with DijkstraAlgorithmComponent {
                val graph = RandomGraph.randomGraph(100, 300)
                DijkstraHelpers.isochrone(1, 20.0)
            }
        }
    }

    test("multilevel dijkstra works on a graph") {
        new RandomGraphComponent with DefaultDijkstraProvider with MultiLevelDijkstraComponent with MultiLevelGraphComponent {
            val dir = Seq(1 -> 2, 2 -> 3, 3 -> 1, 3 -> 4, 4 -> 5, 5 -> 6, 6 -> 7, 7 -> 8, 8 -> 9, 9 -> 7, 9 -> 10, 8 -> 11)
            val regs = Map(1 -> 1, 2 -> 1, 3 -> 1, 4 -> 2, 5 -> 2, 6 -> 2, 7 -> 3, 8 -> 3, 9 -> 3, 10 -> 3, 11 -> 3)
            val undir = dir ++ dir.map(_.swap)
            val weigh = undir.map(x => (x._1, x._2, 1.0))
            val lowlevel = SimpleGraph(weigh, regs)
            val upper = SimpleGraph(
                (3, 4, 1.0),
                (4, 6, 2.0),
                (6, 7, 1.0))
            val iso = MultilevelDijkstra.isochrone(1, 3.1)
            info(iso.keySet.toString)
            assert(iso.keySet == Set(1, 2, 3, 4, 5))
            val iso2 = MultilevelDijkstra.isochrone(1, 6.5)
            info(iso2.toSeq.sortBy(_._1).toString)
            assert(iso2.keySet == Set(1, 2, 3, 6, 7, 8, 9))
        }
    }
}
