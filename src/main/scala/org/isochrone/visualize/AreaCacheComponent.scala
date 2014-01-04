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
            val q = for {
                a1 <- roadNetTables.roadAreas if a1.nodeId.inSet(nds)
                a2 <- roadNetTables.roadAreas if a2.id === a1.id
                a3 <- roadNetTables.roadAreas if a3.nodeId === a2.nodeId
            } yield (a3.nodeId, a3.id, a3.costToCover)
            logger.debug(s"Query = ${q.selectStatement}")
            implicit val s = session
            val retr = q.list
            logger.debug("Done retrieving")
            retr.groupBy(_._1).map {
                case (ndid, lst) => ndid -> (for ((_, aid, cst) <- lst) yield NodeArea(aid, cst))
            }.withDefaultValue(Nil)
        }
    }

    val areaCache = DbAreaCache
}

