package org.isochrone.intersections

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.OsmTableComponent
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.db.DatabaseProvider
import java.sql.Timestamp
import org.isochrone.osm.CostAssignerComponent
import com.typesafe.scalalogging.slf4j.Logging
import com.vividsolutions.jts.geom.LineString

trait IntersectionFinderComponent {
    self: RoadNetTableComponent with OsmTableComponent with DatabaseProvider with CostAssignerComponent =>

    object IntersectionFinder extends Logging {
        def intersectionsIn(top: Double, left: Double, bottom: Double, right: Double) = {
            for {
                e1 <- roadNetTables.roadNet
                e2 <- roadNetTables.roadNet
                if e1.geom @&& makeBox(makePoint(left, bottom), makePoint(right, top)).setSRID(4326)
                if e2.geom @&& makeBox(makePoint(left, bottom), makePoint(right, top)).setSRID(4326)
                if e1.start =!= e2.start && e1.end =!= e2.end
                if e1.start =!= e2.end && e1.end =!= e2.start
                if (e1.geom intersects e2.geom)
            } yield (e1.start, e1.end, e1.geom.asColumnOf[LineString], e2.start, e2.end, e2.geom.asColumnOf[LineString])
        }

        def hasIntersections(top: Double, left: Double, bottom: Double, right: Double) = {
            database.withTransaction { implicit s: Session =>
                intersectionsIn(top, left, bottom, right).firstOption.isDefined
            }
        }

        def removeIntersections(top: Double, left: Double, bottom: Double, right: Double) = {
            database.withTransaction { implicit s: Session =>
                val intersectionQuery = intersectionsIn(top, left, bottom, right)
                logger.debug(s"IntersectionQuery: ${intersectionQuery.selectStatement}")
                val pairs = intersectionQuery.list
                val maxid = Query(roadNetTables.roadNodes.map(_.id).max).firstOption.flatten.getOrElse(0l)
                val collection = new IntersectionCollection(pairs, maxid)
                roadNetTables.roadNodes.insertAll((collection.newForDb.map((x: (Long, Geometry)) => (x._1, 0, x._2)): _*))
                for {
                    (start, end) <- pairs.map(x => (x._1, x._2)).distinct
                } {
                    val nodes = collection.intersectionsForEdge(start, end)
                    val rnq = roadNetTables.roadNet.filter(edg => edg.start === start && edg.end === end)
                    val virt = rnq.map(_.virtual).firstOption.getOrElse(false)
                    rnq.delete
                    logger.debug(s"Creating chain from $start to $end via $nodes")
                    for (Seq(ns, ne) <- (start +: nodes :+ end).sliding(2)) {
                        roadNetTables.roadNet.insert(for {
                            sn <- roadNetTables.roadNodes if sn.id === ns
                            en <- roadNetTables.roadNodes if en.id === ne
                        } yield (sn.id,
                            en.id,
                            if (virt) getNoRoadCost(sn.geom, en.geom) else getRoadCost(sn.geom, en.geom),
                            virt,
                            sn.geom.shortestLine(en.geom).asColumnOf[Geometry]))
                    }

                }
            }

        }
    }

}