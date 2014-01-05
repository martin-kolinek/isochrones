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

trait AreaCacheComponent {
    self: GraphComponentBase =>

    case class NodeArea(areaId: Long, costToCover: Double)

    trait AreaCache {
        def getNodesAreas(nds: Seq[NodeType]): NodeType => List[NodeArea]
    }

    val areaCache: AreaCache
}

trait DbAreaCacheComponent extends AreaCacheComponent with GraphComponentBase {
    self: SessionProviderComponent with RoadNetTableComponent =>
    type NodeType = Long
    object DbAreaCache extends AreaCache with Logging {

        def getNodesAreas(nds: Seq[NodeType]): Map[NodeType, List[NodeArea]] = {
            logger.debug(s"Retrieving nodes, count = ${nds.size}")
            val ndList = "(" + nds.mkString(",") + ")"
            val q = sql"""select a2.node_id, a2.id, a2.cost_to_cover 
                from (select distinct id from "#${roadNetTables.roadAreas.tableName}" where node_id in #$ndList) a1
                    inner join "#${roadNetTables.roadAreas.tableName}" a2 on a1.id = a2.id 
                order by a2.id""".as[(Long, Long, Double)]
            implicit val s = session
            logger.debug("Before retrieving")
            val retr = q.list
            logger.debug(s"Done retrieving (retrieved ${retr.size} rows)")
            retr.groupBy(_._1).map {
                case (ndid, lst) => ndid -> (for ((_, aid, cst) <- lst) yield NodeArea(aid, cst))
            }.withDefaultValue(Nil)
        }
    }

    val areaCache = DbAreaCache
}

