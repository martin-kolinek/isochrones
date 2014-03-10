package org.isochrone.dijkstra

import org.scalatest.FunSuite
import org.isochrone.graphlib._
import org.isochrone.util.DoublePrecision
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.util.RandomGraphComponent
import org.isochrone.util.RandomGraphComponent
import org.isochrone.simplegraph.SimpleGraphWithRegionsComponent

class DijkstraIsochroneTest extends FunSuite {
    test("dijkstra isochrone finds an isochrone in star") {
        new GraphComponent with SimpleGraphComponent with DijkstraAlgorithmComponent {
            type NodeType = Int
            lazy val edges = (1 to 10).map((0, _, 0.5)) ++ (1 to 10).map(x => (x, x + 10, 0.5))
            lazy val graph = SimpleGraph(edges: _*)
            lazy val iso = DijkstraHelpers.isochrone(0, 0.6).toList
            assert(iso.filter(_._1 != 0).map(_._2).forall(q => math.abs(q - 0.1) < 0.01))
        }
    }

    test("dijkstra finds an isochrone on random graph") {
        for (i <- 1 to 3) {
            new RandomGraphComponent with GraphComponent with DijkstraAlgorithmComponent {
                lazy val graph = RandomGraph.randomGraph(100, 300)
                DijkstraHelpers.isochrone(1, 20.0)
            }
        }
    }

    def lowerlevelregs = {
        Map(1 -> 1, 2 -> 1, 3 -> 1, 4 -> 2, 5 -> 2, 6 -> 2, 7 -> 3, 8 -> 3, 9 -> 3, 10 -> 3, 11 -> 3)
    }

    def lowerlevel = {
        val dir = Seq(1 -> 2, 2 -> 3, 3 -> 1, 3 -> 4, 4 -> 5, 5 -> 6, 6 -> 7, 7 -> 8, 8 -> 9, 9 -> 7, 9 -> 10, 8 -> 11)
        val undir = dir ++ dir.map(_.swap)
        undir.map(x => (x._1, x._2, 1.0))
    }

    def upperlevel = Seq(
        (3, 4, 1.0),
        (4, 6, 2.0),
        (6, 7, 1.0))

    test("multilevel dijkstra works on a graph") {
        new SimpleGraphWithRegionsComponent with DijkstraAlgorithmProviderComponent with MultiLevelDijkstraComponent with MultiLevelGraphComponent {
            type NodeType = Int
            val lowlevel = SimpleGraphWithRegions(lowerlevel, lowerlevelregs)
            val upper = SimpleGraphWithRegions(upperlevel: _*)
            val levels = Seq(lowlevel, upper)
            val iso = MultilevelDijkstra.isochrone(Seq(1 -> 0.0), 3.1).toList
            val ndset = iso.map(_.nd).toSet
            assert(ndset.contains(5))
            assert(!ndset.contains(6))
            assert(math.abs(iso.toSeq.filter(_.nd == 5).head.remaining - 0.1) < 0.01)
        }
    }

    test("multilevel dijkstra does not ask for unneeded edges") {
        new MultiLevelGraphComponent with SimpleGraphWithRegionsComponent with DijkstraAlgorithmProviderComponent with MultiLevelDijkstraComponent {
            type NodeType = Int
            var init = true
            def lowlevel = new SimpleGraphWithRegions(lowerlevel, lowerlevelregs, lowerlevelregs.map(_._1 -> (0.0, 0.0)).toMap) {
                override def neighbours(node: Int) = {
                    super.neighbours(node)
                }
            }
            lowlevel.nodes.map(lowlevel.nodeRegion).map(_.map(_.diameter))
            init = false
            def upper = SimpleGraphWithRegions(upperlevel: _*)
            lazy val levels = Seq(lowlevel, upper)
            val iso = MultilevelDijkstra.isochrone(Seq(1 -> 0.0), 6.5).toList
            val ndset = iso.map(_.nd).toSet
            assert(ndset.contains(8))
            assert(ndset.contains(9))
            assert(!ndset.contains(10))
            assert(!ndset.contains(11))
            info(iso.toString)
            assert(iso.filter(x => x.nd == 8 || x.nd == 9).map(x => x.remaining).forall(x => math.abs(x - 0.5) <= 0.001))
        }
    }
}
