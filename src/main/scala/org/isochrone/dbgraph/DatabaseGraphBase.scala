package org.isochrone.dbgraph

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.util.LRUCache
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.mutable.HashMap
import org.isochrone.db.RoadNetTables
import org.isochrone.util.Timing
import org.isochrone.graphlib.GraphWithRegionsType
import org.isochrone.db.BasicRoadNetTables

abstract class DatabaseGraphBase(protected val roadNetTables: BasicRoadNetTables, maxRegions: Int, protected val session: Session) extends Logging {
    protected final val regionMap = new LRUCache[Int, Traversable[Long]]((k, v, m) => {
        val ret = m.size > maxRegions
        if (ret) {
            logger.debug(s"Removing region $k")
            removeRegion(v)
        }
        ret
    })

    private val nodesProps = new HashMap[Long, NodeProperties]

    private val nodesToRegions = new HashMap[Long, Int]

    type NodeProperties

    private var retrievalscntr = 0
    final def retrievals = retrievalscntr

    private def removeRegion(nodes: Traversable[Long]) = for (n <- nodes) {
        nodesToRegions -= n
        nodesProps -= n
    }

    final def propsForNode(nd: Long) = {
        ensureInMemory(nd)
        if (nodesToRegions.contains(nd))
            regionMap.updateUsage(nodesToRegions(nd))
        nodesProps(nd)
    }

    private def regionNodes(rg: Int) = {
        ensureRegion(rg)
        regionMap(rg)
    }

    final def ensureRegion(rg: Int) {
        if (!regionMap.contains(rg))
            retrieveRegion(rg)
    }

    def nodeRegion(node: Long) = {
        ensureInMemory(node)
        nodesToRegions.get(node)
    }

    final def nodesInMemory = nodesProps.size

    private def ensureInMemory(node: Long) {
        if (!nodesProps.isDefinedAt(node)) {
            retrieveNode(node)
        }
    }

    private def retrieveNode(node: Long) {
        retrievalscntr += 1
        val q = roadNetTables.roadNodes.filter(_.id === node).map(_.region)
        q.list()(session).map(x => retrieveRegion(x))
    }

    type QueryType <: Query[_, QueryResult]

    type QueryResult

    def nodePropsFromQueryResult(qrs: List[QueryResult]): Traversable[(Long, NodeProperties)]

    def query(region: Int): QueryType

    private def retrieveRegion(region: Int) {
        Timing.timeLogged(logger, x => s"retrieveRegion($region) took $x") {
            logger.debug(s"Region select: ${query(region).selectStatement}")
            val list = query(region).list()(session)
            val nps = nodePropsFromQueryResult(list)
            regionMap(region) = nps.map(_._1)
            for ((n, _) <- nps) {
                nodesToRegions(n) = region
            }
            nodesProps ++= nps
        }
    }
}