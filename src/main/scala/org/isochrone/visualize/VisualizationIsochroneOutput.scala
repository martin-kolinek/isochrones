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
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait VisualizationIsochroneOutputComponent extends IsochroneOutputComponent with Logging with PosAreaComponent {
    self: IsochronesComputationComponent with AreaInfoComponent with GraphComponentBase with AreaVisualizerComponent =>

    val geomFact2 = new GeometryFactory(new PrecisionModel, 4326)

    def isochroneGeometry: Traversable[Geometry] = {
        val isoList = isochrone.toList
        
        logger.debug(s"Nodes: ${isoList.map(x => x.nd -> x.remaining)}")
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
        logger.info(s"Found all required areas (size = ${allIds.size})")
        val geoms = areaInfoRetriever.getAreaGeometries(allIds)
        val posAreas = areaInfoRetriever.getAreas(areaNodes.keys)
        logger.info("Retrieved data about areas from db")

        val coveredGeomsF = Future {
            logger.debug("Determining coveredGeoms")
            val union = Option(CascadedPolygonUnion.union(allIds.filter(covered).map(geoms)))
            val indiv = union.toTraversable.flatMap(_.individualGeometries)
            indiv.map {
                case poly: Polygon => geomFact2.createPolygon(poly.getExteriorRing.getCoordinates()): Geometry
            }
        }
        coveredGeomsF.foreach(_ => logger.debug("Got covered geoms"))

        val partiallyCoveredF = Future {
            logger.debug("Determining partiallyCovered")
            Option(CascadedPolygonUnion.union(areaNodes.keys.map(geoms)))
        }
        partiallyCoveredF.foreach(_ => logger.debug("Got partially covered"))

        val coveredWithoutPartially = for {
            coveredGeoms <- coveredGeomsF
            partiallyCovered <- partiallyCoveredF
        } yield {
            logger.debug("Determining coveredWithoutPartially")
            coveredGeoms.map(g => (g /: partiallyCovered)(_ difference _))
        }
        coveredWithoutPartially.foreach(_ => logger.debug("Got covered without partially"))

        logger.debug("Determining outGeoms")
        val outGeomsF = Future.sequence(areaNodes.map {
            case (id, nodes) => Future {
                val arGeom = geoms(id)
                areaVisualizer.areaGeom(posAreas(id), arGeom, nodes)
            }
        }).map(_.flatten.flatMap(_.individualGeometries))
        outGeomsF.foreach(_ => logger.debug("Got outGeoms"))

        val result = for {
            out <- outGeomsF
            covWithoutPart <- coveredWithoutPartially
        } yield covWithoutPart ++ out
        result.foreach(_ => logger.info("Got result"))
        Await.result(result, Duration.Inf)
    }
}
