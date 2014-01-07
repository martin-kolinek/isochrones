package org.isochrone.connect

import org.isochrone.db.DatabaseProvider
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.dijkstra.DijkstraProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.util._
import org.isochrone.osm.SpeedCostAssignerComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.graphlib.GraphComponentBase
import com.vividsolutions.jts.geom.Geometry
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.ArgumentParser
import org.isochrone.OptionParserComponent
import org.isochrone.ArgumentParser
import scopt.OptionParser
import org.isochrone.visualize.ApproxEquidistAzimuthProj
import slick.jdbc.StaticQuery.interpolation

trait WalkingEdgesAdderComponent {
    def addWalkingEdges()
}

trait SimpleWalkingEdgesAdderComponent extends WalkingEdgesAdderComponent with GraphComponentBase with Logging {
    self: DatabaseProvider with RoadNetTableComponent with DijkstraAlgorithmComponent with SpeedCostAssignerComponent with MaxCostQuotientComponent =>
    type NodeType = Long
    def addWalkingEdges() {
        database.withTransaction { implicit s: Session =>
            val q = Query(roadNetTables.roadNodes).map(x => (x.id, x.geom)).sortBy(_._1)
            q.elements.zipWithIndex.foreach {
                case ((startid, geom), idx) => {
                    val point = geom.getInteriorPoint
                    val proj = new ApproxEquidistAzimuthProj(point.getX, point.getY)
                    logger.info(s"Processing node $startid (index = $idx)")
                    val dist = maxDistance
                    val box = {
                        val (left, low) = proj.unproject(-dist, -dist)
                        val (right, up) = proj.unproject(dist, dist)
                        makeBox(makePoint(left, low), makePoint(right, up))
                    }
                    logger.debug(s"Looking for nodes within $dist meters")
                    val ndsQuery = for {
                        n <- roadNetTables.roadNodes if n.id === startid
                        n2 <- roadNetTables.roadNodes if n2.geom @&& box
                    } yield n2.id -> getNoRoadCost(n.geom, n2.geom)
                    val nds = ndsQuery.list
                    val nodeSet = nds.view.map(_._1).toSet
                    val dijkstraNodes = DijkstraHelpers.compute(startid).filter(x => nodeSet.contains(x._1)).take(nds.size).toMap
                    logger.debug(s"Dijkstra search returned ${dijkstraNodes.size} nodes")
                    logger.debug(s"Database query returned ${nds.size} nodes")
                    val ndsFilt = nds.filter {
                        case (id, cost) => cost - dijkstraNodes.get(id).getOrElse(Double.PositiveInfinity) < -1E-10 //round off errors
                    }
                    logger.info(s"Adding ${ndsFilt.size} edges")
                    ndsFilt.foreach {
                        case (id, cost) => {
                            logger.debug(s"Adding edge ($startid, $id), cost = $cost, dijkstra = ${dijkstraNodes.get(id).getOrElse(Double.PositiveInfinity)}")
                            val edgQ = for {
                                n1 <- roadNetTables.roadNodes if n1.id === startid
                                n2 <- roadNetTables.roadNodes if n2.id === id
                                if !Query(roadNetTables.roadNet).filter(e => e.start === startid && e.end === id).exists
                            } yield (n1.id, n2.id, getNoRoadCost(n1.geom, n2.geom), true, n1.geom.shortestLine(n2.geom).asColumnOf[Geometry])
                            roadNetTables.roadNet.insert(edgQ)
                        }
                    }

                }
                case _ => assert(false)
            }
        }
    }
}

trait MaxCostQuotientComponent {
    def maxDistance: Double
}

trait ConfigMaxCostQuotientComponent extends OptionParserComponent with MaxCostQuotientComponent {
    self: ArgumentParser =>

    val maxDistanceLens = registerConfig(500.0)

    lazy val maxDistance = maxDistanceLens.get(parsedConfig)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) {
        super.parserOptions(pars)
        pars.opt[Double]("max-distance").action((x, c) => maxDistanceLens.set(c)(x))
    }
}