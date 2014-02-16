package org.isochrone.visualize

import org.isochrone.compute.IsochroneOutputComponent
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion
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
import com.vividsolutions.jts.geom.Polygon
import scala.collection.JavaConversions._

trait VisualizationIsochroneOutputComponent extends IsochroneOutputComponent with Logging with PosAreaComponent {
    self: IsochronesComputationComponent with AreaInfoComponent with GraphComponentBase with AreaVisualizerComponent with OnlyLinesComponent =>

    val geomFact2 = new GeometryFactory(new PrecisionModel, 4326)

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

        val areaNeighbours = areaNodes.map {
            case (arid, nds) => arid -> nds.view.map(_.nd).flatMap(ndArs).map(_.areaId).force
        }

        logger.info("Grouped to areas and found neighbouring areas")
        val allIds = (areaNeighbours.keys ++ areaNeighbours.values.flatten).toSet
        logger.info(s"Found all required areas (size = ${allIds.size}")
        val geoms = areaInfoRetriever.getAreaGeometries(allIds)
        logger.info("Retrieved area geometries")
        val posAreas = areaInfoRetriever.getAreas(areaNodes.keys)
        logger.info("Retrieved area information")
        val coveredUnion = CascadedPolygonUnion.union(allIds.filter(covered).map(geoms))
        logger.info("Found union of covered area")
        val coveredPolys = coveredUnion.individualGeometries
        logger.info("Extracted individual geometrie")
        val coveredGeoms = coveredPolys.map {
            case poly: Polygon => geomFact2.createPolygon(poly.getExteriorRing.getCoordinates()): Geometry
        }
        logger.info("Constructed polygons from shells")
        val outGeoms = (for ((id, nodes) <- areaNodes) yield {
            val arGeom = geoms(id)
            areaVisualizer.areaGeom(posAreas(id), arGeom, nodes)
        }).flatten.flatMap(_.individualGeometries)
        logger.info("Retrieved info from AreaVisualizer")
        Some(CascadedPolygonUnion.union(coveredGeoms ++ outGeoms)).filterNot(_.isEmpty)
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
