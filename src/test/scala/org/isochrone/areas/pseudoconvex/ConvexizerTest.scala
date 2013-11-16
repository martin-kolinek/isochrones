package org.isochrone.areas.pseudoconvex

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.dijkstra.DefaultDijkstraProvider

class ConvexizerTest extends FunSuite {
    test("convexizer works") {
        new HertelMehlhortModConvexizerComponent with GraphComponentBase with DefaultDijkstraProvider {
            type NodeType = Int
            val lst = List(1, 2, 3, 4, 5, 6)
            val costs = (for (Seq(a, b) <- (6 :: lst).sliding(2)) yield Seq((a, b) -> 1.0, (b, a) -> 1.0)).flatten.toSeq
            val ptWithPos = PointWithPosition(1, List(0, 0))
            val area = Area(lst.map(PointWithPosition(_, List(0, 0))),
                Map(costs: _*))
            val diagonals = List(EdgeWithCost(Set(3, 6), 0.1), EdgeWithCost(Set(2, 6), 10), EdgeWithCost(Set(3, 5), 8))
            val result = convexizer.convexize(area, diagonals)
            assert(result.size == 1)
            assert(result.head.nds == Set(3, 6))
        }
    }
}