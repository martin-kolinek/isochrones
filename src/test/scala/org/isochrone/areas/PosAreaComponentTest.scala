package org.isochrone.areas

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponentBase
import spire.syntax.normedVectorSpace._
import spire.std.double._
import spire.std.seq._

trait PtsComponent extends PosAreaComponent with GraphComponentBase {
    type NodeType = Int

    val pts = List(PointWithPosition(0, List(0, 2)),
        PointWithPosition(1, List(2, 2)),
        PointWithPosition(2, List(2, 0)),
        PointWithPosition(3, List(0, 0)),
        PointWithPosition(4, List(1, 1)),
        PointWithPosition(5, List(0.5, 1)),
        PointWithPosition(6, List(1.5, 1)))

}

class PosAreaComponentTest extends FunSuite {

    test("middle vector works") {
        new PtsComponent {
            import ListPositionImplicit._

            def check(pos: Position, chk: Position) = (pos - chk.normalize).norm < 0.001

            val mid1 = List(1.0, 0.0).middleVect(List(-1.0, 0.0))
            assert(check(mid1, List(0.0, 1.0)))
            val mid2 = List(1.0, 0.0).middleVect(List(0.0, 1.0))
            assert(check(mid2, List(1.0, 1.0)))
            val mid3 = List(-1.0, 0.0).middleVect(List(0.0, -1.0))
            info(mid3.toString)
            assert(check(mid3, List(-1, -1)))
        }
    }

    test("simple area is linearring") {
        new PtsComponent {
            val simple = PosArea(0, List(0, 1, 2, 3).map(pts), Map())
            info(simple.toLinearRing.toString)
            assert(simple.toLinearRing.isValid)
        }
    }

    test("shrinking an area creates a linearring") {
        new PtsComponent {
            val area = PosArea(0, List(0, 1, 4, 1, 2, 3).map(pts), Map())
            assert(!area.toLinearRing.isValid)
            val shr = area.shrink(0.01)
            info(shr.toLinearRing.toString)
            assert(shr.toLinearRing.isValid)
        }
    }

    test("shrinking a more difficult area creates a linearring") {
        new PtsComponent {
            val area = PosArea(0, List(0, 1, 4, 5, 4, 6, 4, 1, 2, 3).map(pts), Map())
            assert(!area.toLinearRing.isValid)
            val shrinked = area.shrink(0.01)
            val lr = shrinked.toLinearRing
            info(lr.toString)
            assert(lr.isValid)
        }
    }

    test("normalize works") {
        new PosAreaComponent with GraphComponentBase {
            type NodeType = Int
            val area = PosArea(0, List(PointWithPosition(1, List(48.0, 17.0)),
                PointWithPosition(1, List(48.0, 16.99)),
                PointWithPosition(1, List(47.99, 16.99))), Map())
            val norm = area.normalize
            info(norm.toString)
            assert(norm.points.map(_.pos).forall(x => x.norm <= 1))
        }
    }
}