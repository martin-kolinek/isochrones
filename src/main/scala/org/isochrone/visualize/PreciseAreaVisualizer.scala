package org.isochrone.visualize

import org.isochrone.graphlib.GraphComponentBase
import scala.collection.JavaConversions._
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
import com.vividsolutions.jts.geom.LineString
import com.vividsolutions.jts.geom.LinearRing
import org.isochrone.OptionParserComponent
import org.isochrone.ArgumentParser
import scopt.OptionParser
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion
import com.vividsolutions.jts.geom.GeometryCollection
import com.typesafe.scalalogging.slf4j.Logging

trait PreciseAreaVisualizerComponent extends AreaVisualizerComponentTypes with CircleDrawingComponent {
    self: GraphComponentBase with SpeedCostAssignerComponent with CirclePointsCountComponent with AzimuthalProjectionComponent =>
    private val geomFact = new GeometryFactory(new PrecisionModel, 4326)

    trait PreciseAreaVisualizer extends AreaVisualizer with Logging {
        def areaGeom(area: PosArea, areaGeom: Geometry, nodes: List[IsochroneNode]): Traversable[Geometry] = {
            logger.debug(s"Nodes $nodes")
            logger.debug(s"PosArea $area")
            val nodePositions = area.points.map(p => p.nd -> p.pos).toMap
            def getNodeGeom(nd: IsochroneNode): Traversable[Geometry] = {
                val edges = area.neighbourMap(nd.nd).map(n2 => (nd, n2, area.cost(nd.nd, n2)))
                logger.debug(s"Edges: $edges")
                edges.map((edgeGeom _).tupled)
            }

            def edgeGeom(nd: IsochroneNode, nd2: NodeType, cst: Double) = {
                val List(cx, cy) = nodePositions(nd.nd)
                val proj = projectionForPoint(cx, cy)
                val List(x, y) = nodePositions(nd2)
                val circ = CircleDrawing.circle(cx, cy, noRoadCostToMeters(nd.remaining))
                val ret = if (nd.remaining <= cst) {
                    val projected2 = vector.tupled(proj.project(x, y))
                    val List(adjx, adjy) = projected2 :* nd.remaining / cst
                    val (unprojx, unprojy) = proj.unproject(adjx, adjy)
                    val pt = geomFact.createPoint(new Coordinate(unprojx, unprojy))
                    logger.debug(s"Pt: $pt")
                    val coll = new GeometryCollection(Array(circ, pt), geomFact)
                    logger.debug(s"CircPoint $nd")
                    coll.convexHull
                } else {
                    val circ2 = CircleDrawing.circle(x, y, noRoadCostToMeters(nd.remaining - cst))
                    val coll = new GeometryCollection(Array(circ, circ2), geomFact)
                    logger.debug(s"TwoCircs $nd")
                    coll.convexHull
                }
                logger.debug(s"edgeGeom ret: $ret")
                ret
            }
            val nodeGeoms = nodes.flatMap(getNodeGeom).flatMap(_.individualGeometries).toList
            logger.debug(s"node geometries: $nodeGeoms")
            Option(CascadedPolygonUnion.union(nodeGeoms))
            //nodeGeoms
        }
    }
}

trait SomePreciseAreaVisualizer extends AreaVisualizerComponent with PreciseAreaVisualizerComponent {
    self: GraphComponent with NodePositionComponent with SpeedCostAssignerComponent with CirclePointsCountComponent with AzimuthalProjectionComponent =>
    val areaVisualizer = new PreciseAreaVisualizer {}
}
