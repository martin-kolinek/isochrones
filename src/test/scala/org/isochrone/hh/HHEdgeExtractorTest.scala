package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponent
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.GraphType

class HHEdgeExtractorTest extends FunSuite {
    test("Highway hierarchies pick edge on line") {
        val comp = new FirstPhaseComponentImpl with SecondPhaseComponent with SimpleGraphComponent with DijkstraAlgorithmProviderComponent with GraphComponentBase with FirstPhaseParameters {
            def maverickThresholdMultiplier = Double.PositiveInfinity
            type NodeType = Int
            val graph = {
                val edg = (1 to 25).map(x => (x, x + 1))
                SimpleGraph.undirOneCost(edg: _*)
            }

            val neighbourhoods = new NeighbourhoodSizes[NodeType] {
                def neighbourhoodSize(nd: NodeType) = 3.1
            }

            val tree = firstPhase(graph, neighbourhoods).nodeTree(1)
            info(tree.toString)
            val got = secondPhase(graph, neighbourhoods).extractHighwayEdges(tree)
            info(got.toString)
            assert(got.toSet === Set(5 -> 6, 6 -> 5, 4 -> 5, 5 -> 4))
        }
    }

    test("hhextract  works on sample graph") {
        val comp = new SimpleGraphComponent with FirstPhaseComponentImpl with DijkstraAlgorithmProviderComponent with FirstPhaseParameters with NeighbourhoodSizeFinderComponent with NeighbourhoodCountComponent with SecondPhaseComponent {
            val maverickThresholdMultiplier = Double.PositiveInfinity
            def neighbourhoodCount = 3
            val graph: GraphType[NodeType] = SimpleGraph.undir(
                (1, 2, 75),
                (1, 3, 125),
                (2, 4, 60),
                (3, 4, 70),
                (2, 9, 210),
                (9, 10, 150),
                (10, 11, 70),
                (11, 12, 60),
                (10, 13, 120),
                (12, 13, 65),
                (4, 5, 200),
                (5, 7, 65),
                (7, 12, 190),
                (5, 6, 60),
                (6, 8, 65),
                (7, 8, 120),
                (3, 27, 445),
                (6, 27, 280),
                (12, 14, 300),
                (14, 24, 265),
                (14, 15, 90),
                (15, 20, 150),
                (15, 16, 160),
                (16, 18, 70))

            val ns = new NeighbourhoodSizes[Long] {
                val finder = neighSizeFinder(graph)
                def neighbourhoodSize(nd: Long) = finder.findNeighbourhoodSize(nd, neighbourhoodCount)
            }
            val fp = firstPhase(graph, ns)
            val tree = fp.nodeTree(4)
            val shouldParentMap = Map(11 -> 10, 10 -> 9, 9 -> 2, 2 -> 4, 1 -> 2, 2 -> 4, 3 -> 4, 27 -> 3, 12 -> 7, 7 -> 5, 5 -> 4, 8 -> 6, 6 -> 5)
            val shouldChildMap = shouldParentMap.groupBy(_._2).map(x => x._1 -> x._2.map(_._1).toSet) //yeah I'm lazy
            assert(tree.parentMap.toList.sorted === shouldParentMap.toList.sorted)
            assert(tree.childMap.mapValues(_.toSet) === shouldChildMap)
            assert(tree.withinStartNeighbourhood === Set(1, 2, 3, 4))
            val sp = secondPhase(graph, ns)
            val edges = sp.extractHighwayEdges(tree)
            val oneWay = edges.filter(x => x._1 < x._2).sorted
            val should = List(2 -> 9, 9 -> 10, 7 -> 12, 4 -> 5, 5 -> 7, 3 -> 27).sorted
            assert(oneWay === should)
        }
    }
}