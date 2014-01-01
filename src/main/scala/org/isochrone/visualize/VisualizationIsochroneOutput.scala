package org.isochrone.visualize

import org.isochrone.compute.IsochroneOutputComponent
import org.isochrone.compute.IsochronesComputationComponent
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import org.isochrone.graphlib.NodePositionComponent
import com.vividsolutions.jts.geom.Coordinate
import java.awt.geom.Point2D
import org.isochrone.osm.SpeedCostAssignerComponent
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphComponent
import spire.syntax.normedVectorSpace._
import spire.std.seq._
import spire.std.double._
import org.isochrone.util._
import org.isochrone.ArgumentParser

trait VisualizationIsochroneOutputComponent extends IsochroneOutputComponent {
    self: IsochronesComputationComponent with AreaCacheComponent with AreaGeometryCacheComponent with NodePositionComponent with SpeedCostAssignerComponent with GraphComponent with CirclePointsCountComponent =>

    private val geomFact = new GeometryFactory(new PrecisionModel, 4326)

    def isochroneGeometry = {
        val isoList = isochrone.toList
        val covered = (for {
            nd <- isoList
            arnd <- areaCache.getNodeAreas(nd.nd) if nd.remaining >= arnd.costToCover
        } yield arnd.areaId).toSet

        val areaNodes = (for {
            nd <- isoList
            arnd <- areaCache.getNodeAreas(nd.nd) if !covered.contains(arnd.areaId)
        } yield {
            (nd, arnd)
        }).groupBy(_._2.areaId).map {
            case (arid, lst) => arid -> lst.map(_._1)
        }
        areaNodes.map((getAreaGeom _).tupled)
    }

    def getAreaGeom(arid: Long, nodes: List[IsochroneNode]): Geometry = {
        val nodeGeoms = nodes.map(getNodeGeom(nodes.map(_.nd).toSet))
        areaGeomCache.getAreaGeom(arid).intersection(nodeGeoms.reduce(_ union _))
    }

    def getNodeGeom(arnds: Set[NodeType])(nd: IsochroneNode): Geometry = {
        val geoms = graph.neighbours(nd.nd).filter(x => arnds.contains(x._1)).map(x => edgeGeom(nd, x._1, x._2))
        geoms.reduce(_ union _)
    }

    def edgeGeom(nd: IsochroneNode, nd2: NodeType, cst: Double) = {
        val List(cx, cy) = vector.tupled(nodePos.nodePosition(nd.nd))
        val proj = new EquidistantAzimuthalProjection(cx, cy)
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

trait CirclePointsCountComponent {
    def circlePointCount: Int
}

trait ConfigCirclePointsCountComponent extends CirclePointsCountComponent with VisualizationConfigComponent {
    self: ArgumentParser =>

    def circlePointCount = circlePointsLens.get(parsedConfig)
}

trait VisualizationConfigComponent extends OptionParserComponent {
    val circlePointsLens = registerConfig(40)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Int]("circle-points").text("Number of points in circles (default = 40)").action((x, c) => circlePointsLens.set(c)(x))
    }
}

object VisualizationUtil {
    private val geomFact = new GeometryFactory(new PrecisionModel, 4326)

    def circle(cx: Double, cy: Double, radius: Double, numPoints: Int) = {
        val proj = new EquidistantAzimuthalProjection(cx, cy)
        val angFrac = 2 * math.Pi / numPoints
        val coords = for (ang <- (0 until numPoints).map(_ * angFrac)) yield {
            val (x, y) = proj.unproject(math.cos(ang) * radius, math.sin(ang) * radius)
            new Coordinate(x, y)
        }
        geomFact.createPolygon((coords :+ coords.head).toArray)
    }

    //r1 - radius of left circle
    //r2 - radius of right circle
    //d - distance between circles
    //taken from internet
    private def circleIntersections(r1: Double, r2: Double, d: Double) = {
        val x = (d * d - r2 * r2 + r1 * r1) / (2 * d)
        val y = {
            val first = -d + r2 - r1
            val second = -d - r2 + r1
            val third = -d + r2 + r1
            val fourth = d + r2 + r1
            val a = (1 / d) * math.sqrt(first * second * third * fourth)
            a / 2
        }
        Seq(vector(x, y), vector(x, -y))
    }

    def circleIntersection(c1: List[Double], c2: List[Double], r1: Double, r2: Double): Seq[List[Double]] = {
        val direction = (c2 - c1).normalize
        val normal = vector(direction.y, -direction.x).normalize
        val d = (c2 - c1).norm
        for (List(x, y) <- circleIntersections(r1, r2, d))
            yield c1 + (direction :* x) + (normal :* y)
    }
}