package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponent
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.dijkstra.DijkstraAlgorithmComponent

class HHEdgeExtractorTest extends FunSuite {
    test("Highway hierarchies pick edge on line") {
        val comp = new FirstPhaseComponent with SecondPhaseComponent with GraphComponent with SimpleGraphComponent with DefaultDijkstraProvider with DijkstraAlgorithmComponent with NeighbourhoodSizeComponent {
            type NodeType = Int
            val graph = {
                val edg = (1 to 25).map(x => (x, x + 1))
                SimpleGraph.undirOneCost(edg: _*)
            }

            val neighbourhoods = new NeighbourhoodSizes[NodeType] {
                def neighbourhoodSize(nd: NodeType) = 3.1
            }

            val tree = FirstPhase.nodeTree(1)
            info(tree.toString)
            val got = SecondPhase.extractHighwayEdges(tree)
            info(got.toString)
            assert(got.toSet === Set(5 -> 6, 6 -> 5, 4 -> 5, 5 -> 4))
        }
    }
}