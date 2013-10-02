package org.isochrone.dijkstra

import org.scalatest.FunSuite
import org.isochrone.graphlib._
import org.isochrone.util.DoublePrecision
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.util.RandomGraphComponent
import org.isochrone.util.RandomGraphComponent

class DijkstraIsochroneTest extends FunSuite {
    test("dijkstra isochrone finds an isochrone in star") {
        new SimpleGraphComponent with DefaultDijkstraProvider with DijkstraAlgorithmComponent with GraphComponent {
            type NodeType = Int
            val edges = (1 to 10).map((0, _, 0.5)) ++ (1 to 10).map(x => (x, x + 10, 0.5))
            val graph = SimpleGraph(edges: _*)
            val iso = DijkstraHelpers.isochrone(0, 0.6).toList
            assert(iso.map(_.start).toSet == (1 to 10).toSet)
            assert(iso.map(_.end).toSet == (11 to 20).toSet)
            assert(iso.map(_.part).forall(q => math.abs(q - 0.2) < 0.01))
        }
    }

    test("dijkstra finds an isochrone on random graph") {
        for (i <- 1 to 3) {
            new RandomGraphComponent with DefaultDijkstraProvider with DijkstraAlgorithmComponent with GraphComponent {
                val graph = RandomGraph.randomGraph(100, 300)
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
        new SimpleGraphComponent with DefaultDijkstraProvider with MultiLevelDijkstraComponent with MultiLevelGraphComponent {
        	type NodeType = Int
            val lowlevel = new SimpleGraph(lowerlevel, lowerlevelregs)
            val upper = SimpleGraph(upperlevel: _*)
            val levels = Seq(lowlevel, upper)
            val iso = MultilevelDijkstra.isochrone(Seq(1 -> 0.0), 3.1)
            assert(iso.map(x => x.start -> x.end).toSet == Set(5 -> 6))
            assert(math.abs(iso.toSeq.head.part - 0.1) < 0.01)
            //assert(iso2.keySet == Set(1, 2, 3, 6, 7, 8, 9))
        }
    }

    test("multilevel dijkstra does not ask for unneeded edges") {
        new SimpleGraphComponent with DefaultDijkstraProvider with MultiLevelDijkstraComponent with MultiLevelGraphComponent {
            type NodeType = Int
            var init = true
            val lowlevel = new SimpleGraph(lowerlevel, lowerlevelregs) {
                override def neighbours(node: Int) = {
                    if (node == 5 && !init)
                        println(s"Asked for neighbours of $node")
                    super.neighbours(node)
                }
            }
            lowlevel.nodes.map(lowlevel.nodeRegion).map(_.map(_.diameter))
            init = false
            val upper = SimpleGraph(upperlevel: _*)
            val levels = Seq(lowlevel, upper)
            val iso = MultilevelDijkstra.isochrone(Seq(1 -> 0.0), 6.5)
            assert(iso.map(x => x.start -> x.end).toSet == Set(9 -> 10, 8 -> 11))
            assert(iso.map(x => x.part).forall(x => math.abs(x - 0.5) <= 0.001))
        }
    }
}
