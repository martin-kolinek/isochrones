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
import org.isochrone.areas.PosAreaComponent
import org.isochrone.graphlib.GraphComponentBase

trait VisualizationIsochroneOutputComponent extends IsochroneOutputComponent with Logging with PosAreaComponent {
    self: IsochronesComputationComponent with AreaInfoComponent with GraphComponentBase with AreaVisualizerComponent =>

    def isochroneGeometry = {
        val isoList = isochrone.toList
        logger.info(s"Got isochrone (size = ${isoList.size}), computing geometry")
        val ndArs = areaInfoRetriever.getNodesAreas(isoList.map(_.nd))

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

        val geoms = areaInfoRetriever.getAreaGeometries(areaNodes.keys)

        val posAreas = areaInfoRetriever.getAreas(areaNodes.keys)

        (for ((id, nodes) <- areaNodes) yield {
            areaVisualizer.areaGeom(posAreas(id), geoms(id), nodes)
        }).flatten
    }

    def areaGeometries(ars: Traversable[Long]): Map[Long, Geometry] = {
        ???
    }

    def areas(ids: Seq[Long]): Map[Long, PosArea] = ???
}



