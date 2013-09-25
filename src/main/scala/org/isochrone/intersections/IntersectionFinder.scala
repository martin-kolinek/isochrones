package org.isochrone.intersections

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.OsmTableComponent
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.db.DatabaseProvider
import java.sql.Timestamp

trait IntersectionFinderComponent {
    self: RoadNetTableComponent with OsmTableComponent with DatabaseProvider =>

    object IntersectionFinder {
        def intersectionsIn(top: Double, left: Double, bottom: Double, right: Double) = {
            for {
                n1s <- osmTables.nodes
                n1e <- osmTables.nodes
                n2s <- osmTables.nodes
                n2e <- osmTables.nodes
                e1 <- roadNetTables.roadNet if n1s.id === e1.start && n1e.id === e1.end
                e2 <- roadNetTables.roadNet if n2s.id === e2.start && n2e.id === e2.end
                if n1s.geom @&& makeBox(makePoint(left, top), makePoint(right, bottom)).setSRID(4326) ||
                    n1e.geom @&& makeBox(makePoint(left, top), makePoint(right, bottom)).setSRID(4326)
                if n2s.geom @&& makeBox(makePoint(left, top), makePoint(right, bottom)).setSRID(4326) ||
                    n2e.geom @&& makeBox(makePoint(left, top), makePoint(right, bottom)).setSRID(4326)
                if e1.start =!= e2.start && e1.end =!= e2.end
                if e1.start =!= e2.end && e1.end =!= e2.start
                if (n1s.geom shortestLine n1e.geom).asColumnOf[Geometry].setSRID(4326) intersects
                    (n2s.geom shortestLine n2e.geom).asColumnOf[Geometry].setSRID(4326)
            } yield (n1s.id, n1s.geom, n1e.id, n1e.geom, n2s.id, n2s.geom, n2e.id, n2e.geom)
        }

        def hasIntersections(top: Double, left: Double, bottom: Double, right: Double) = {
            database.withSession { implicit s: Session =>
                intersectionsIn(top, left, bottom, right).firstOption.isDefined
            }
        }

        def removeIntersections(top: Double, left: Double, bottom: Double, right: Double) = {
            database.withSession { implicit s: Session =>
                val pairs = intersectionsIn(top, left, bottom, right).list
                val maxid = Query(Query(osmTables.nodes).map(_.id).max).firstOption.flatten.getOrElse(0l)
                val collection = new IntersectionCollection(pairs, maxid)
                osmTables.nodes.insertAll((collection.newForDb.map(
                    x => (x._1,
                        x._2.asInstanceOf[Geometry],
                        0,
                        0,
                        new Timestamp(System.currentTimeMillis),
                        0l,
                        Map[String, String]())): _*))
                for {
                    (start, _, end, _, _, _, _, _) <- pairs
                } {
                    val nodes = collection.intersectionsForEdge(start, end)
                    Query(roadNetTables.roadNet).filter(edg => edg.start === start && edg.end === end).delete
                    for (Seq(ns, ne) <- (start +: nodes :+ end).sliding(2)) {
                        roadNetTables.roadNet.insert(for {
                            sn <- osmTables.nodes if sn.id === ns
                            en <- osmTables.nodes if en.id === ne
                        } yield (sn.id, en.id, (sn.geom distanceSphere en.geom).asColumnOf[Double]))
                    }

                }
            }

        }
    }

}