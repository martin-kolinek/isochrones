package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponent
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.graphlib.GraphComponentBase

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
}