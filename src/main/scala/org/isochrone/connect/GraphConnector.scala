package org.isochrone.connect

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.HigherLevelRoadNetTableComponent
import com.typesafe.scalalogging.slf4j.Logging
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.db.RoadNetTables
import scala.annotation.tailrec

trait GraphConnectorComponent {
    self: RoadNetTableComponent with HigherLevelRoadNetTableComponent with DatabaseProvider =>

    object GraphConnector extends Logging {
        def findClosestNodes(st: Set[Int])(implicit s: Session) = {
            def query(limit: Float) = {
                val q = for {
                    n1 <- roadNetTables.roadNodes if n1.region.inSet(st)
                    n2 <- roadNetTables.roadNodes if n1.geom.distance(n2.geom) < limit && !n2.region.inSet(st)
                } yield (n1.id, n2.id, n1.geom.distance(n2.geom))
                q.sortBy(_._3).map(x => (x._1, x._2))
            }

            val nds = for (dif <- Stream.iterate(0.000976563f, 15)(_ * 2))
                yield query(dif).firstOption
            nds.find(_.isDefined).flatten
        }

        def findUnconnected(implicit s: Session) = {
            val regions = Query(roadNetTables.roadNodes).groupBy(_.region).map(_._1).list

            val regionJoints = for {
                n1 <- roadNetTables.roadNodes
                n2 <- roadNetTables.roadNodes if n1.region =!= n2.region
                rh <- higherRoadNetTables.roadNet if n1.id === rh.start && n2.id === rh.end
            } yield (n1.region, n2.region)

            val ds = (new DisjointSets(regions) /: regionJoints.list)((s, j) => s.union(j._1, j._2))

            ds.allSets.toSeq
        }

        def insertEdge(a: Long, b: Long, tbls: RoadNetTables)(implicit s: Session) {
            tbls.roadNet.insert(for {
                n1 <- tbls.roadNodes if n1.id === a
                n2 <- tbls.roadNodes if n2.id === b
            } yield (n1.id, n2.id, 0.0, false, n1.geom.shortestLine(n2.geom).asColumnOf[Geometry]))
        }

        def tryInsertNode(a: Long)(implicit s: Session) {
            if (higherRoadNetTables.roadNodes.filter(_.id === a).firstOption.isEmpty) {
                higherRoadNetTables.roadNodes.insert(roadNetTables.roadNodes.filter(_.id === a).map(_.*))
            }
        }

        @tailrec
        def connectAll(implicit s: Session): Unit = {
            val uncon = findUnconnected
            if (uncon.size > 1) {
                val next = uncon.minBy(_.size)
                logger.info(s"Connecting $next to the rest of the graph (${uncon.size} remaining)")
                val closest = findClosestNodes(next)
                closest match {
                    case None => throw new Exception(s"Could not find closest pair of nodes for regions $next")
                    case Some((a, b)) => {
                        insertEdge(a, b, roadNetTables)
                        insertEdge(b, a, roadNetTables)
                        tryInsertNode(a)
                        tryInsertNode(b)
                        insertEdge(a, b, higherRoadNetTables)
                        insertEdge(b, a, higherRoadNetTables)
                    }
                }
                logger.info("Done connecting that")
                connectAll
            } else Unit
        }

        def connectGraph() {
            database.withTransaction { implicit s: Session =>
                connectAll(s: Session)
            }
        }
    }
}