package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent

class HigherDescendLimitFinderTest extends FunSuite {
    test("HigherDescendLimitFinderTest") {
        val comp = new NonDbHigherDescendLimitFinderComponent with GraphComponentBase with SimpleGraphComponent with DijkstraAlgorithmProviderComponent {
            type NodeType = Int
            val grp = SimpleGraph.undirOneCost(1 -> 3, 2 -> 3, 1 -> 4, 2 -> 4, 4 -> 5)
            val neighSize = new NeighbourhoodSizes[Int] {
                def neighbourhoodSize(nd: Int) = 1.5
            }
            val descLim = new DescendLimitProvider[Int] {
                def descendLimit(nd: Int) = nd match {
                    case 1 => 3.0
                    case 2 => 3.5
                    case 3 => 3.5
                    case 4 => 3.5
                    case 5 => 3.5
                }
            }
            object DescLimFinder extends NonDbHigherDescendLimitFinder
            val result = DescLimFinder.getDescendLimits(grp, neighSize, descLim, Map(1 -> 2.0, 2 -> 5.0, 4 -> 5.0, 3 -> 2), List(1, 2))
            assert(!result.contains(5))
            assert(result(4) === 5.0)
            assert(result(3) === 4.5)
            assert(result(1) === 3.0)
            assert(result(2) === 5.0)
        }

    }
}