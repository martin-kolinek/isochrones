package org.isochrone.compute

import org.scalatest.FunSuite
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.NodePosition

class PointIsochroneOutputTest extends FunSuite {
    test("PointIsochroneOutput works") {
        val comp = new PointIsochroneOutputComponent with IsochronesComputationComponent with NodePositionComponent {
            type NodeType = Int
            def isochrone = List(IsochroneEdge(1, 2, 0.5), IsochroneEdge(4, 7, 0.4), IsochroneEdge(5, 6, 1), IsochroneEdge(1, 3, 0))
            val nodePos = new NodePosition[NodeType] {
                def nodePosition(nd: Int) = (nd.toDouble, nd.toDouble + 1)
            }
        }

        val res = comp.isochroneGeometry.map(g => (g.getX * 10000).toInt -> (g.getY * 10000).toInt).toSet
        val exp = Set(15000 -> 25000, 52000 -> 62000, 60000 -> 70000, 10000 -> 20000)
        assert(res == exp)
    }
}