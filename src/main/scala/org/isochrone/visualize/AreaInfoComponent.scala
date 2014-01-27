package org.isochrone.visualize

import org.isochrone.db.SessionProviderComponent
import org.isochrone.util.LRUCache
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.ArgumentParser
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import com.typesafe.scalalogging.slf4j.Logging
import slick.jdbc.StaticQuery.interpolation
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.areas.PosAreaComponent
import org.isochrone.util._

trait AreaInfoComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    case class NodeArea(areaId: Long, costToCover: Double)

    trait AreaInfoRetriever {
        def getNodesAreas(nds: Traversable[NodeType]): NodeType => List[NodeArea]
        def getAreaGeometries(ars: Traversable[Long]): Long => Geometry
        def getAreas(ars: Traversable[Long]): Long => PosArea
    }

    val areaInfoRetriever: AreaInfoRetriever
}

trait DbAreaInfoComponent extends AreaInfoComponent with GraphComponentBase {
    self: SessionProviderComponent with RoadNetTableComponent =>
    type NodeType = Long
    object DbAreaInfoRetriever extends AreaInfoRetriever with Logging {
        implicit private val s = session
        def getNodesAreas(nds: Traversable[NodeType]): Map[NodeType, List[NodeArea]] = {
            logger.debug(s"Retrieving nodes, count = ${nds.size}")
            val ndList = "(" + nds.mkString(",") + ")"
            val q = sql"""select a2.node_id, a2.id, a2.cost_to_cover 
                from (select distinct id from "#${roadNetTables.roadAreas.tableName}" where node_id in #$ndList) a1
                    inner join "#${roadNetTables.roadAreas.tableName}" a2 on a1.id = a2.id 
                order by a2.id""".as[(Long, Long, Double)]
            logger.debug("Before retrieving")
            val retr = q.list
            logger.debug(s"Done retrieving (retrieved ${retr.size} rows)")
            retr.groupBy(_._1).map {
                case (ndid, lst) => ndid -> (for ((_, aid, cst) <- lst) yield NodeArea(aid, cst))
            }.withDefaultValue(Nil)
        }

        def getAreaGeometries(ars: Traversable[Long]) = {
            Query(roadNetTables.areaGeoms).filter(_.id.inSet(ars)).toMap
        }

        def getAreas(ars: Traversable[Long]) = {
            val q = for {
                a <- roadNetTables.roadAreas if a.id.inSet(ars)
                n <- roadNetTables.roadNodes if a.nodeId === n.id
                a2 <- roadNetTables.roadAreas if a.id === a2.id
                e <- roadNetTables.roadNet if e.start === n.id && e.end === a2.nodeId
            } yield (a.id, a.sequenceNo, n.id, n.geom, e.end, e.cost)
            val sorted = q.sortBy(x => (x._1, x._2))
            case class Row(areaId: Long, seqNo: Int, nodeId: Long, geom: Geometry, end: Long, cost: Double)
            val fromDb = sorted.list.view.map(Row.tupled)
            def extractPoints(rows: Seq[Row]): List[PointWithPosition] = {
                val map = rows.groupBy(_.nodeId).map {
                    case (id, rows) => {
                        val point = rows.head.geom.getInteriorPoint
                        id -> PointWithPosition(id, vector(point.getX, point.getY))
                    }
                }
                rows.map(_.nodeId).split(_ != _).map(_.head).toList.map(map)
            }

            def extractCosts(rows: Seq[Row]): Map[(Long, Long), Double] = {
                rows.groupBy(x => (x.nodeId, x.end)).map {
                    case (edg, rows) => edg -> rows.head.cost
                }.toMap
            }

            fromDb.groupBy(_.areaId).map {
                case (id, lst) => {
                    val points = extractPoints(lst)
                    val costs = extractCosts(lst)
                    id -> PosArea(id, points, costs)
                }
            }
        }

    }

    val areaInfoRetriever = DbAreaInfoRetriever
}

