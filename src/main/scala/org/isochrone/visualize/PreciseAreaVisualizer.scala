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
import com.vividsolutions.jts.geom.LineString
import com.vividsolutions.jts.geom.LinearRing
import org.isochrone.OptionParserComponent
import org.isochrone.ArgumentParser
import scopt.OptionParser

trait PreciseAreaVisualizerComponent extends AreaVisualizerComponentTypes with CircleDrawingComponent {
    self: GraphComponent with NodePositionComponent with SpeedCostAssignerComponent with CirclePointsCountComponent with AzimuthalProjectionComponent with OnlyLinesComponent =>
    private val geomFact = new GeometryFactory(new PrecisionModel, 4326)

    trait PreciseAreaVisualizer extends AreaVisualizer {
        def areaGeom(area: PosArea, areaGeom: Geometry, nodes: List[IsochroneNode]): Traversable[Geometry] = {
            val nodeGeoms = nodes.flatMap(getNodeGeom(nodes.map(_.nd).toSet))
            if (nodeGeoms.isEmpty)
                None
            else {
                val geom = areaGeom.intersection(nodeGeoms.reduce(_ union _))
                if (onlyLines) {
                    val bound = geom.getBoundary
                    (0 until bound.getNumGeometries).map(bound.getGeometryN).view.map {
                        case lr: LinearRing => geomFact.createLineString(lr.getCoordinates())
                        case ls: LineString => ls
                        case _ => throw new Exception("Not a line returned by getBoundary")
                    }.map(_.difference(areaGeom.getBoundary))
                } else {
                    Some(geom)
                }
            }
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
            val proj = projectionForPoint(cx, cy)
            val List(x, y) = vector.tupled(nodePos.nodePosition(nd2))
            val circ = CircleDrawing.circle(cx, cy, noRoadCostToMeters(nd.remaining))
            if (nd.remaining <= cst) {
                val projected2 = vector.tupled(proj.project(x, y))
                val List(adjx, adjy) = projected2 :* nd.remaining / cst
                val (unprojx, unprojy) = proj.unproject(adjx, adjy)
                val pt = geomFact.createPoint(new Coordinate(unprojx, unprojy))
                (circ union pt).convexHull
            } else {
                val circ2 = CircleDrawing.circle(x, y, noRoadCostToMeters(nd.remaining - cst))
                (circ union circ2).convexHull
            }
        }
    }
}

trait OnlyLinesComponent {
    def onlyLines: Boolean
}

trait ConfigOnlyLinesComponent extends OptionParserComponent with OnlyLinesComponent {
    self: ArgumentParser =>

    lazy val onlyLinesLens = registerConfig(false)
    lazy val onlyLines = onlyLinesLens.get(parsedConfig)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) {
        super.parserOptions(pars)
        pars.opt[Boolean]("only-lines").text("output only lines, not whole polygons (default = false)").action((x, c) => onlyLinesLens.set(c)(x))
    }
}

trait SomePreciseAreaVisualizer extends AreaVisualizerComponent with PreciseAreaVisualizerComponent {
    self: GraphComponent with NodePositionComponent with SpeedCostAssignerComponent with CirclePointsCountComponent with AzimuthalProjectionComponent with OnlyLinesComponent =>
    val areaVisualizer = new PreciseAreaVisualizer {}
}