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
import com.typesafe.scalalogging.slf4j.Logging

trait VisualizationIsochroneOutputComponent extends IsochroneOutputComponent with Logging {
    self: IsochronesComputationComponent with AreaCacheComponent with AreaGeometryCacheComponent with NodePositionComponent with SpeedCostAssignerComponent with GraphComponent with CirclePointsCountComponent with AreaVisualizerComponent =>

    def isochroneGeometry = {
        val isoList = isochrone.toList
        logger.info(s"Got isochrone (size = ${isoList.size}), computing geometry")
        val ndArs = areaCache.getNodesAreas(isoList.map(_.nd))
        val covered = (for {
            nd <- isoList
            arnd <- ndArs(nd.nd) if nd.remaining >= arnd.costToCover
        } yield arnd.areaId).toSet
        logger.info(s"Found covered (size = ${covered.size})")
        val areaNodes = (for {
            nd <- isoList
            arnd <- ndArs(nd.nd) if !covered.contains(arnd.areaId)
        } yield {
            (nd, arnd)
        }).groupBy(_._2.areaId).map {
            case (arid, lst) => arid -> lst.map(_._1)
        }
        areaNodes.map((areaVisualizer.areaGeom _).tupled).flatten
    }
}



