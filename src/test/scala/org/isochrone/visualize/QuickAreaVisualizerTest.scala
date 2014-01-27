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
import com.vividsolutions.jts.io.WKTReader
import org.isochrone.osm.SpeedCostAssignerComponent

class QuickAreaVisualizerTest extends FunSuite with MustMatchers {

    object AreaComponent extends PosAreaComponent with GraphComponentBase with AreaGeometryFinderComponent with IsochroneComputerComponentTypes with QuickAreaVisualizerComponent with CirclePointsCountComponent with TranslatingProjectionComponent with SpeedCostAssignerComponent {
        def circlePointCount = 20

        def noRoadSpeed = 1.0 / 1000.0
        def roadSpeed = 10.0 / 1000.0

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
        val got@List(b) = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0),
            Some(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.5, 0), true)),
            Some(areaVis.ResultPoint(vector(0.7, 0), true), areaVis.ResultPoint(vector(0, 1), false)))
        assert(b.pt.x > 0.5 && b.pt.x < 0.7)
        assert(b.pt.y > 0)
    }

    test("connectAroundEdge works with one overtaking other") {
        val got = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0),
            Some(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.0, 1.0), false)),
            Some(areaVis.ResultPoint(vector(0.7, 0), true), areaVis.ResultPoint(vector(0.1, 0.9), false)))
        got must equal(Nil)
    }

    test("connectAroundEdge works with not touching") {
        val got@List(b, c) = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0),
            Some(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.7, 0), true)),
            Some(areaVis.ResultPoint(vector(0.3, 0), true), areaVis.ResultPoint(vector(0, 1), false)))
        assert((b.pt, c.pt) === (vector(0.7, 0), vector(0.3, 0)))
        assert(got.forall(_.onEdge))
    }

    test("connectAroundEdge works with only touching") {
        val got = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0),
            Some(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.5, 0), true)),
            Some(areaVis.ResultPoint(vector(0.5, 0), true), areaVis.ResultPoint(vector(0, 1), false)))
        info(got.size.toString)
        assert((got.size == 1) === !got.head.onEdge)
    }

    test("connectAroundNode works with acute angles") {
        import areaVis.ResultPoint
        val got@(List(a)) = areaVis.connectAroundNode(vector(0, 0))(ResultPoint(vector(4, 1), false), ResultPoint(vector(0, 1), false))(ResultPoint(vector(1, 0), false), ResultPoint(vector(1, 4), false))
        assert((a.pt - vector(1.0, 1.0)).norm < 0.0001)
        assert(a.onEdge === false)
    }

    test("connectAroundEdge works with only first edge") {
        val got = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0),
            Some(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.5, 0), true)),
            None)

        got must equal(List(areaVis.ResultPoint(vector(0.5, 0), true)))
    }

    test("connectAroundEdge works with only second edge") {
        val got = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0),
            None,
            Some(areaVis.ResultPoint(vector(0.3, 0), true), areaVis.ResultPoint(vector(0, 1), false)))

        got must equal(List(areaVis.ResultPoint(vector(0.3, 0), true)))
    }

    test("connectAroundNode works with obtuse angles") {
        import areaVis.interiorPoint
        val got = areaVis.connectAroundNode(vector(0, 0))(interiorPoint(vector(4, 1)), interiorPoint(vector(0, 1)))(interiorPoint(vector(-1, 0)), interiorPoint(vector(-1, -4)))
        info(got.toString)
        assert((got.head.pt - vector(0.0, 1)).norm < 0.0001)
        assert((got.last.pt - vector(-1.0, 0)).norm < 0.0001)
        assert(got.forall(x => math.abs((x.pt - vector(0.0, 0)).norm - 1.0) < 0.0001))
        assert(got.forall(!_.onEdge))
    }

    test("connectAroundEdge with overlapping lines") {
        val got = areaVis.connectAroundEdge(vector(0, 0), vector(1, 0),
            Some(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.0, 1.0), false)),
            Some(areaVis.ResultPoint(vector(1, 1), false), areaVis.ResultPoint(vector(0.0, 1.0), false)))
        got must equal(Nil)
    }

    test("resultPoints work") {
        info(areaVis.resultPoints.toString)
        assert(areaVis.resultPoints.size === 9)
    }

    test("createLine does create tangent lines") {
        val l1 = (areaVis.extractBack _).tupled(areaVis.createLine(pointMap(1), pointMap(2)).get)
        val l2 = (areaVis.extractForward _).tupled(areaVis.createLine(pointMap(2), pointMap(1)).get)
        val jl1 = areaVis.createJtsLine(l1._1.pt, l1._2.pt)
        val jl2 = areaVis.createJtsLine(l2._1.pt, l2._2.pt)
        info(jl1.toString)
        info(jl2.toString)
        assert(!(jl1 intersects jl2))
    }

    test("QuickAreaVisualizer works") {
        val wkt = """MULTILINESTRING ((10 5, 9.543373756578255 0.456626243421745, 1.3698787302652349 1.3698787302652349, 0 15), (20 5, 19.543373756578255 0.4566262434217448, 15 0, 10.456626243421745 0.456626243421745, 10 5))"""
        assert(areaVis.result.isDefined)
        info(areaVis.result.get.toString)
        assert(areaVis.result.get.isValid)
        assert(areaVis.result.get.toString == wkt)
    }
}