package org.isochrone.areas.pseudoconvex

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponentBase

class TriangulatorTest extends FunSuite {
    test("Poly2TriTriangulator triangulates") {
        new Poly2TriTriangulatorComponent with GraphComponentBase {
            type NodeType = Int
            val ar = PosArea(0, List(PointWithPosition(1, List(0.1, 2.3)),
                PointWithPosition(2, List(1.89, 2.1)),
                PointWithPosition(3, List(1.9, 0.01)),
                PointWithPosition(4, List(0.03, 0.03))), Map())
            val edgs = triangulator.triangulate(ar)
            info(edgs.toString)
            assert(edgs.size == 1)
            val st = Set(edgs.head._1, edgs.head._2)
            assert(st == Set(1, 3) || st == Set(2, 4))
        }

    }
}