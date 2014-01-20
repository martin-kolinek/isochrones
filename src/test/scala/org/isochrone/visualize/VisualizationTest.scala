package org.isochrone.visualize

import org.scalatest.FunSuite
import java.awt.geom.Point2D
import org.isochrone.compute.IsochronesComputationComponent
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.graphlib.GraphComponent
import org.isochrone.osm.SpeedCostAssignerComponent
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
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

class VisualizationTest extends FunSuite {
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
        val geom = VisualizationUtil.circle(48, 17, 100, 4)
        assert(geom.isValid)
        val geom2 = VisualizationUtil.circle(48, 17, 100, 100)
        assert(geom2.isValid)
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

    test("visualizer creates a geometry") {
        val comp = new IsochronesComputationComponent with AreaCacheComponent with AreaGeometryCacheComponent with NodePositionComponent with GraphComponent with SpeedCostAssignerComponent with SomePreciseAreaVisualizer with VisualizationIsochroneOutputComponent with SimpleGraphComponent with DefaultDijkstraProvider with CirclePointsCountComponent {
            def circlePointCount = 10
            type NodeType = Int
            val areaCache: AreaCache = new AreaCache {
                def getNodesAreas(nds: Seq[NodeType]) = x => List(NodeArea(1, 10))
            }
            val geomFact = new GeometryFactory(new PrecisionModel, 4326)
            val areaGeomCache: AreaGeometryCache = new AreaGeometryCache {
                def getAreaGeom(ar: Long) = geomFact.createPolygon((Seq(1, 2, 3, 4, 1)).map(nodePos.nodePosition).
                    map(x => new Coordinate(x._1, x._2)).toArray)
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
        val wkt = """MULTIPOLYGON (((48.02 17, 48 17, 48 17.005134696220185, 48.00174167085455 17.005134696220185, 48.02 17)), ((48.1 17.01540408866056, 48.1 17, 48.04 17, 48.094774987436345 17.01540408866056, 48.1 17.01540408866056)))"""
        val reader = new WKTReader
        val lst = List(reader.read(wkt))
        info(outgeom.toString)
        assert(outgeom.forall(_.isValid))
        assert(lst == outgeom)
    }
}