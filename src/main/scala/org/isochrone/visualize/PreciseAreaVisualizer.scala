package org.isochrone.visualize

import org.isochrone.graphlib.GraphComponentBase
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.graphlib.GraphComponent
import spire.syntax.normedVectorSpace._
import spire.std.seq._
import spire.std.double._
import org.isochrone.util._
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.osm.SpeedCostAssignerComponent
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Coordinate

trait PreciseAreaVisualizerComponent extends AreaVisualizerComponentTypes {
    self: GraphComponent with NodePositionComponent with AreaGeometryCacheComponent with SpeedCostAssignerComponent with CirclePointsCountComponent =>
    private val geomFact = new GeometryFactory(new PrecisionModel, 4326)

    trait PreciseAreaVisualizer extends AreaVisualizer {
        def areaGeom(arid: Long, nodes: List[IsochroneNode]): Option[Geometry] = {
            val nodeGeoms = nodes.flatMap(getNodeGeom(nodes.map(_.nd).toSet))
            if (nodeGeoms.isEmpty)
                None
            else
                Some(areaGeomCache.getAreaGeom(arid).intersection(nodeGeoms.reduce(_ union _)))
        }

        def getNodeGeom(arnds: Set[NodeType])(nd: IsochroneNode): Option[Geometry] = {
            val geoms = graph.neighbours(nd.nd).filter(x => arnds.contains(x._1)).map(x => edgeGeom(nd, x._1, x._2))
            if (geoms.isEmpty)
                None
            else
                Some(geoms.reduce(_ union _))
        }

        def edgeGeom(nd: IsochroneNode, nd2: NodeType, cst: Double) = {
            val List(cx, cy) = vector.tupled(nodePos.nodePosition(nd.nd))
            val proj = new ApproxEquidistAzimuthProj(cx, cy)
            val List(x, y) = vector.tupled(nodePos.nodePosition(nd2))
            val circ = VisualizationUtil.circle(cx, cy, noRoadCostToMeters(nd.remaining), circlePointCount)
            if (nd.remaining <= cst) {
                val projected2 = vector.tupled(proj.project(x, y))
                val List(adjx, adjy) = projected2 :* nd.remaining / cst
                val (unprojx, unprojy) = proj.unproject(adjx, adjy)
                val pt = geomFact.createPoint(new Coordinate(unprojx, unprojy))
                (circ union pt).convexHull
            } else {
                val circ2 = VisualizationUtil.circle(x, y, noRoadCostToMeters(nd.remaining - cst), circlePointCount)
                (circ union circ2).convexHull
            }
        }
    }
}

trait SomePreciseAreaVisualizer extends AreaVisualizerComponent with PreciseAreaVisualizerComponent {
    self: GraphComponent with NodePositionComponent with AreaGeometryCacheComponent with SpeedCostAssignerComponent with CirclePointsCountComponent =>
    val areaVisualizer = new PreciseAreaVisualizer {}
}