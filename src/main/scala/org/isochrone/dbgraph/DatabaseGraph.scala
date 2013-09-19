package org.isochrone.dbgraph

import org.isochrone.util.LRUCache
import scala.collection.mutable.HashMap
import scala.slick.driver.BasicDriver.simple._
import org.isochrone.graphlib.GraphType
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.SessionProviderComponent
import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphWithRegionsComponent
import org.isochrone.graphlib.GraphWithRegionsType

trait DatabaseGraphComponent extends GraphWithRegionsComponent {
    self: RoadNetTableComponent with SessionProviderComponent =>
    class DatabaseGraph(maxRegions: Int) extends GraphWithRegionsType[Long, Int] {
        type Region = Int
        type Node = Long
        private val regions = new LRUCache[Region, Traversable[Node]]((k, v, m) => {
            val ret = m.size > maxRegions
            if (ret)
                removeRegion(v)
            ret
        })

        private val neigh = new HashMap[Node, Traversable[(Node, Double)]]

        private val nodesToRegions = new HashMap[Node, Region]

        private var retrievalscntr = 0
        def retrievals = retrievalscntr
        
        def removeRegion(nodes: Traversable[Node]) = for (n <- nodes) {
            nodesToRegions -= n
            neigh -= n
        }

        def nodeRegion(node: Node) = {
            ensureRegion(node)
            nodesToRegions.get(node)
        }

        def nodesInMemory = neigh.size

        def ensureRegion(node: Node) {
            if (!nodesToRegions.isDefinedAt(node))
                retrieveNode(node)
        }

        def ensureInMemory(node: Node) {
            if (!neigh.isDefinedAt(node)) {
                retrieveNode(node)
            }
        }

        def neighbours(node: Node) = {
            ensureInMemory(node)
            if (nodesToRegions.contains(node))
                regions.updateUsage(nodesToRegions(node))
            neigh.getOrElse(node, Seq())
        }
        
        def retrieveNode(node: Node) {
            retrievalscntr += 1
            val q = roadNetTables.roadNodes.filter(_.id === node).map(_.region)
            q.list()(session).map(retrieveRegion)
        }

        def retrieveRegion(region: Region) {
            val startJoin = roadNetTables.roadNodes leftJoin roadNetTables.roadNet on ((n, e) => n.id === e.start)
            val q = for ((n, e) <- startJoin.sortBy(_._1.id) if n.region === region) yield n.id ~ e.end.? ~ e.cost.?
            val list = q.list()(session)
            regions(region) = list.map(_._1)
            val map = list.groupBy(_._1)
            for ((k, v) <- map) {
                neigh(k) = v.collect { case (st, Some(en), Some(c)) => (en, c) }
                nodesToRegions(k) = region
            }
        }

        def nodes = roadNetTables.roadNodes.map(_.id).list()(session)

        lazy val regionDiameters = {
            val q = for {
                r <- roadNetTables.roadRegions
            } yield r.id -> r.diameter
            q.list()(session).toMap
        }

        def nodeEccentricity(n: Long) = (for {
            r <- nodeRegion(n)
            d <- regionDiameters.get(r)
        } yield d).getOrElse(Double.PositiveInfinity)
    }
    
    type NodeType = Long
    type RegionType = Int
    
    val graph = new DatabaseGraph(500)
}