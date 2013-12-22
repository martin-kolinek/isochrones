package org.isochrone.visualize

import org.isochrone.compute.IsochroneOutputComponent
import org.isochrone.compute.IsochronesComputationComponent
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import org.isochrone.graphlib.NodePositionComponent
import com.vividsolutions.jts.geom.Coordinate
import java.awt.geom.Point2D

trait VisualizationIsochroneOutputComponent extends IsochroneOutputComponent {
    self: IsochronesComputationComponent with AreaCacheComponent with AreaGeometryCacheComponent with NodePositionComponent =>

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

        ???
    }

    def getNodeGeom(nd: IsochroneNode) = {
        val (x, y) = nodePos.nodePosition(nd.nd)
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
}