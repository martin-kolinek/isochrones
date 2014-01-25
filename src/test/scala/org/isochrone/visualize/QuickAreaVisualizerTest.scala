package org.isochrone.visualize

import org.scalatest.FunSuite
import org.isochrone.areas.PosAreaComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.util._
import spire.std.double._
import spire.std.seq._
import spire.syntax.normedVectorSpace._
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.GeometryFactory
import org.isochrone.areas.AreaGeometryFinderComponent
import org.isochrone.compute.IsochroneComputerComponentTypes
import org.scalatest.matchers.MustMatchers

class QuickAreaVisualizerTest extends FunSuite with MustMatchers {

    object AreaComponent extends PosAreaComponent with GraphComponentBase with AreaGeometryFinderComponent with IsochroneComputerComponentTypes with QuickAreaVisualizerComponent with CirclePointsCountComponent with TranslatingProjectionComponent {
        def circlePointCount = 20

        type NodeType = Int

        val pointMap = {
            val posTuples: List[(Double, Double)] = List((0, 0), (10, 0), (20, 0), (20, 30), (0, 30), (10, 10))
            val positions = posTuples.map(vector.tupled)
            val ptnums = 1 to 6
            (ptnums zip positions).map(PointWithPosition.tupled).map(x => x.nd -> x).toMap
        }

        val area = {
            val points = List(1, 5, 4, 3, 2, 6, 2).map(pointMap)
            val lengths: Map[(NodeType, NodeType), Double] = Map((1, 2) -> 1,
                (2, 6) -> 1,
                (2, 3) -> 1,
                (3, 4) -> 3,
                (4, 5) -> 2,
                (5, 1) -> 3)
            val lengthsTwoWay = lengths ++ lengths.map { case (k, v) => k.swap -> v }
            PosArea(1, points, lengthsTwoWay)
        }

        val areaGeom = AreaGeometryFinder.areaGeometry(area)

        val isoNodes = List((1, 1.5), (2, 0.5), (3, 0.5)).map(IsochroneNode.tupled)

        val areaVis = new AreaVisGeom(area, areaGeom, isoNodes)

    }

    import AreaComponent._

    test("createLine back works") {
        val line = areaVis.createLine(pointMap(1), pointMap(2))
        line match {
            case None => assert(false)
            case Some((starts, ends)) => {
                val extracted@(start, end) = areaVis.extractBack(starts, ends)
                val both = Seq(start, end)
                info(extracted.toString)
                assert(both.forall(_.pt.y > 0))
                assert(start.pt.x > end.pt.x)
                (start.pt - pointMap(2).pos).norm must be(0.5 plusOrMinus 0.0001)
                (end.pt - pointMap(1).pos).norm must be(1.5 plusOrMinus 0.0001)
            }
        }
    }
    test("createLine forward works") {
        val line = areaVis.createLine(pointMap(2), pointMap(1))
        line match {
            case None => assert(false)
            case Some((starts, ends)) => {
                val extracted@(start, end) = areaVis.extractForward(starts, ends)
                val both = Seq(start, end)
                info(extracted.toString)
                end.pt.x must be(5.0 plusOrMinus 0.00001)
                end.pt.y must be(0.0 plusOrMinus 0.00001)
                (start.pt - pointMap(2).pos).norm must be(0.5 plusOrMinus 0.00001)
                assert(start.pt.x > end.pt.x)
            }
        }
    }

    test("connectAroundEdge works with intersection") {
        val got@List(b) = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0))(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.5, 0), true))(areaVis.ResultPoint(vector(0.7, 0), true), areaVis.ResultPoint(vector(0, 1), false))
        assert(b.pt.x > 0.5 && b.pt.x < 0.7)
        assert(b.pt.y > 0)
    }

    test("connectAroundEdge works with one overtaking other") {
        val got = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0))(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.0, 1.0), false))(areaVis.ResultPoint(vector(0.7, 0), true), areaVis.ResultPoint(vector(0.1, 0.9), false))
        got must equal(Nil)
    }

    test("connectAroundEdge works with not touching") {
        val got@List(b, c) = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0))(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.7, 0), true))(areaVis.ResultPoint(vector(0.3, 0), true), areaVis.ResultPoint(vector(0, 1), false))
        assert((b.pt, c.pt) === (vector(0.7, 0), vector(0.3, 0)))
        assert(got.forall(_.onEdge))
    }

    test("connectAroundNode works with acute angles") {
        val got@(List(a)) = areaVis.connectAroundNode(vector(0, 0))(vector(4, 1), vector(0, 1))(vector(1, 0), vector(1, 4))
        assert((a - vector(1.0, 1.0)).norm < 0.0001)
    }

    test("connectAroundNode works with obtuse angles") {
        val got = areaVis.connectAroundNode(vector(0, 0))(vector(4, 1), vector(0, 1))(vector(-1, 0), vector(-1, -4))
        info(got.toString)
        assert((got.head - vector(0.0, 1)).norm < 0.0001)
        assert((got.last - vector(-1.0, 0)).norm < 0.0001)
        assert(got.forall(x => math.abs((x - vector(0.0, 0)).norm - 1.0) < 0.0001))
    }
}