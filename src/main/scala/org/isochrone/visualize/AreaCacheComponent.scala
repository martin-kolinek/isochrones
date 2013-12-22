package org.isochrone.visualize

import org.isochrone.db.SessionProviderComponent
import org.isochrone.util.LRUCache
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableComponent

trait AreaCacheComponent {
    self: GraphComponentBase =>

    case class NodeArea(areaId: Long, costToCover: Double)

    trait AreaCache {
        def getNodeAreas(nd: NodeType): List[NodeArea]
    }

    val areaCache: AreaCache
}

trait DbAreaCacheComponent extends AreaCacheComponent with GraphComponentBase {
    self: SessionProviderComponent with RoadNetTableComponent =>
    type NodeType = Long
    class DbAreaCache(maxSize: Int) extends AreaCache {

        private var maxRetrievedCount = 0
        private val cache = new LRUCache[NodeType, List[NodeArea]]((k, v, m) => m.size > math.max(maxSize, maxRetrievedCount))

        def ensureNode(nd: NodeType) = {
            if (!cache.contains(nd))
                retrieveNode(nd)
            assert(cache.contains(nd))
        }

        def rememberedNodes = cache.size

        def retrieveNode(nd: NodeType) = {
            val q = for {
                a1 <- roadNetTables.roadAreas if a1.nodeId === nd
                a2 <- roadNetTables.roadAreas if a2.id === a1.id
                a3 <- roadNetTables.roadAreas if a3.nodeId === a2.nodeId
            } yield (a3.nodeId, a3.id, a3.costToCover)
            implicit val s = session
            val retr = q.list
            maxRetrievedCount = math.max(retr.size, maxRetrievedCount)
            retr.groupBy(_._1).map {
                case (ndid, lst) => {
                    cache(ndid) = for ((_, aid, cst) <- lst) yield NodeArea(aid, cst)
                }
            }
        }

        def getNodeAreas(nd: NodeType) = {
            ensureNode(nd)
            cache(nd)
        }
    }
}
