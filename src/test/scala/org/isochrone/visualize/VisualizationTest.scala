package org.isochrone.visualize

import org.scalatest.FunSuite
import java.awt.geom.Point2D
import org.isochrone.compute.IsochronesComputationComponent
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.graphlib.GraphComponent
import org.isochrone.osm.SpeedCostAssignerComponent
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.graphlib.NodePosition
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate
import org.isochrone.ArgumentParser
import spire.syntax.normedVectorSpace._
import spire.std.double._
import spire.std.seq._
import org.isochrone.util._
import com.vividsolutions.jts.io.WKTReader
import org.scalatest.matchers.MustMatchers

class VisualizationTest extends FunSuite with MustMatchers {

    test("equidistant azimuthal projection works") {
        val proj = new EquidistantAzimuthalProjection(48, 17)
        info(proj.unproject(0, 0).toString)
        assert(proj.unproject(0, 0) == (48, 17))
        val (x, y) = proj.unproject(1, 1)
        val (px, py) = proj.project(x, y)
        info((x, y).toString)
        info((px, py).toString)
        val (shouldx, shouldy) = (48.000009404119396, 17.000008993203462)
        assert(math.abs(shouldx - x) < 0.000000001)
        assert(math.abs(shouldy - y) < 0.000000001)
        assert(math.abs(px - 1) < 0.000000001)
        assert(math.abs(py - 1) < 0.000000001)
    }

    test("approximate equidistant azimuthal projection works") {
        val comp = new ApproxEquidistAzimuthProjComponent {}
        import comp._
        val proj = new ApproxEquidistAzimuthProj(48, 17)
        info(proj.unproject(0, 0).toString)
        assert(proj.unproject(0, 0) == (48, 17))
        val (x, y) = proj.unproject(1, 1)
        val (px, py) = proj.project(x, y)
        info((x, y).toString)
        info((px, py).toString)
        val (shouldx, shouldy) = (48.000009404119396, 17.000008993203462)
        assert(math.abs(shouldx - x) < 0.000001)
        assert(math.abs(shouldy - y) < 0.000001)
        assert(math.abs(px - 1) < 0.000001)
        assert(math.abs(py - 1) < 0.000001)
    }

    test("VisualizationUtil creates valid geometry") {
        new CircleDrawingComponent with CirclePointsCountComponent with AzimuthalProjectionComponent with TranslatingProjectionComponent {
            def circlePointCount = 4
            val geom = CircleDrawing.circle(48, 17, 100)
            assert(geom.isValid)
        }
        new CircleDrawingComponent with CirclePointsCountComponent with AzimuthalProjectionComponent with TranslatingProjectionComponent {
            def circlePointCount = 100
            val geom2 = CircleDrawing.circle(48, 17, 100)
            assert(geom2.isValid)
        }
    }

    test("circle intersection works") {
        val c1 = vector(10.0, 10.0)
        val c2 = vector(8.0, 3.0)
        val ints = VisualizationUtil.circleIntersection(c1, c2, 5, 6)
        assert(ints.forall { v =>
            math.abs((c1 - v).norm - 5.0) < 0.00001 &&
                math.abs((c2 - v).norm - 6.0) < 0.00001
        })
    }

    test("arc works") {
        new CircleDrawingComponent with CirclePointsCountComponent with TranslatingProjectionComponent {
            def circlePointCount = 100
            val arc = CircleDrawing.arc(5, 4, 8, math.Pi / 8, (6 * math.Pi) / 4.0 + math.Pi / 8)
            assert(arc.size === 76)
            arc.foreach(x => (x - vector(5.0, 4)).norm must be(8.0 plusOrMinus 0.00001))
        }
    }

    test("arc circle works") {
        new CircleDrawingComponent with CirclePointsCountComponent with TranslatingProjectionComponent {
            def circlePointCount = 100
            val arc = CircleDrawing.arc(5, 4, 8, 0, 2 * math.Pi)
            assert(arc.size === 101)
            arc.foreach(x => (x - vector(5.0, 4)).norm must be(8.0 plusOrMinus 0.00001))
        }
    }

    test("circle intersection first one in left -> right is top (that is has higher y)") {
        val c1 = vector(0.0, 0.0)
        val c2 = vector(1.0, 0.0)
        val ints = VisualizationUtil.circleIntersection(c1, c2, 3, 3)
        assert(ints.head.y > 0)
        assert(ints.tail.head.y < 0)
        val ints2 = VisualizationUtil.circleIntersection(c2, c1, 3, 3)
        assert(ints2.head.y < 0)
        assert(ints2.tail.head.y > 0)

        val c3 = vector(0.0, 1.0)
        val ints3 = VisualizationUtil.circleIntersection(c1, c3, 3, 3)
        assert(ints3.head.x < 0)
        assert(ints3.tail.head.x > 0)
        val ints4 = VisualizationUtil.circleIntersection(c3, c1, 3, 3)
        assert(ints4.head.x > 0)
        assert(ints4.tail.head.x < 0)
    }

    test("visualizer creates a geometry") {
        val comp = new IsochronesComputationComponent with AreaInfoComponent with NodePositionComponent with GraphComponent with SpeedCostAssignerComponent with SomePreciseAreaVisualizer with VisualizationIsochroneOutputComponent with SimpleGraphComponent with DijkstraAlgorithmProviderComponent with CirclePointsCountComponent with ApproxEquidistAzimuthProjComponent {
            def circlePointCount = 10
            def onlyLines = false
            type NodeType = Int
            private val geomFact = new GeometryFactory(new PrecisionModel, 4326)
            val areaInfoRetriever = new AreaInfoRetriever {
                def getNodesAreas(nds: Traversable[NodeType]) = x => List(NodeArea(1, 10))
                def getAreaGeometries(ars: Traversable[Long]) = x => {
                    geomFact.createPolygon((Seq(1, 2, 3, 4, 1)).map(nodePos.nodePosition).
                        map(x => new Coordinate(x._1, x._2)).toArray)
                }

                def getAreas(ars: Traversable[Long]) = x => {
                    val points = List(1, 2, 3, 4).map { id =>
                        PointWithPosition(id, vector.tupled(nodePos.nodePosition(id)))
                    }
                    val costs = (for {
                        a <- 1 to 4
                        b <- 1 to 4
                    } yield (a, b) -> 0.05).toMap
                    PosArea(x, points, costs)
                }
            }
            val graph = SimpleGraph.undirCost(0.05)(1 -> 2, 2 -> 3, 3 -> 4, 4 -> 1)
            def isochrone: Traversable[IsochroneNode] = Seq(IsochroneNode(1, 0.01), IsochroneNode(2, 0.03))
            val nodePos = new NodePosition[NodeType] {
                def nodePosition(nd: NodeType) = nd match {
                    case 1 => (48, 17)
                    case 2 => (48.1, 17)
                    case 3 => (48.1, 17.1)
                    case 4 => (48, 17.1)
                    case _ => (0, 0)
                }
            }
            def noRoadSpeed: Double = 60
            def roadSpeed: Double = 200
        }
        val outgeom = comp.isochroneGeometry.toList
        val expected = Set("""POLYGON ((47.99825832914545 16.994865303779815, 47.99544024650557 16.99682658321402, 47.99436383472024 17, 48 17.02, 48.00439982840385 17.00438715621188, 48.02 17, 48.00174167085455 16.994865303779815, 47.99825832914545 16.994865303779815))""", """POLYGON ((48.094774987436345 16.98459591133944, 48.04 17, 48.086800514788465 17.013161468635644, 48.1 17.060000000000002, 48.11690849583928 17, 48.1136792604833 16.990479749642056, 48.10522501256366 16.98459591133944, 48.094774987436345 16.98459591133944))""")
        info(outgeom.toString)
        assert(outgeom.forall(_.isValid))
        assert(expected === outgeom.map(_.toString).toSet)
    }
}