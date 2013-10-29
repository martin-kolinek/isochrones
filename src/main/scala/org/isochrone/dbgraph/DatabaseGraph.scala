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
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.graphlib.NodePosition
import org.isochrone.graphlib.GraphWithRegionsComponentBase
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.MultiLevelGraphComponent
import org.isochrone.db.MultiLevelRoadNetTableComponent
import org.isochrone.db.RoadNetTables
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.util.Timing

trait DatabaseGraphComponent extends GraphWithRegionsComponentBase {
    self: SessionProviderComponent =>
    class DatabaseGraph(roadNetTables: RoadNetTables, maxRegions: Int) extends GraphWithRegionsType[Long, RegionType] with NodePosition[NodeType] with Logging {
        private val regionMap = new LRUCache[RegionType, Traversable[NodeType]]((k, v, m) => {
            val ret = m.size > maxRegions
            if (ret) {
                logger.debug(s"Removing region $k")
                removeRegion(v)
            }
            ret
        })

        def regions = regionDiameters.map(_._1)

        private val neigh = new HashMap[NodeType, Traversable[(NodeType, Double)]]

        private val nodePos = new HashMap[NodeType, (Double, Double)]

        private val nodesToRegions = new HashMap[NodeType, RegionType]

        private var retrievalscntr = 0
        def retrievals = retrievalscntr

        def removeRegion(nodes: Traversable[NodeType]) = for (n <- nodes) {
            nodesToRegions -= n
            neigh -= n
            nodePos -= n
        }

        def regionNodes(rg: RegionType) = {
            ensureRegion(rg)
            regionMap(rg)
        }

        def ensureRegion(rg: RegionType) {
            if (!regionMap.contains(rg))
                retrieveRegion(rg)
        }

        def nodePosition(nd: Long) = {
            ensureRegion(nd)
            nodePos(nd)
        }

        def nodeRegion(node: NodeType) = {
            ensureRegion(node)
            nodesToRegions.get(node)
        }

        def nodesInMemory = neigh.size

        def ensureRegion(node: NodeType) {
            if (!nodesToRegions.isDefinedAt(node))
                retrieveNode(node)
        }

        def ensureInMemory(node: NodeType) {
            if (!neigh.isDefinedAt(node)) {
                retrieveNode(node)
            }
        }

        def neighbours(node: NodeType) = {
            ensureInMemory(node)
            if (nodesToRegions.contains(node))
                regionMap.updateUsage(nodesToRegions(node))
            neigh.getOrElse(node, Seq())
        }

        def retrieveNode(node: NodeType) {
            retrievalscntr += 1
            val q = roadNetTables.roadNodes.filter(_.id === node).map(_.region)
            q.list()(session).map(x => retrieveRegion(x))
        }

        def retrieveRegion(region: RegionType) {
            Timing.timeLogged(logger, x => s"retrieveRegion($region) took $x") {
                val startJoin = roadNetTables.roadNodes leftJoin roadNetTables.roadNet on ((n, e) => n.id === e.start)
                val q = for ((n, e) <- startJoin.sortBy(_._1.id) if n.region === region) yield n.id ~ e.end.? ~ e.cost.? ~ n.geom
                logger.debug(s"Region select: ${q.selectStatement}")
                val list = q.list()(session)
                regionMap(region) = list.map(_._1)
                nodePos ++= list.map(x => (x._1, (x._4.getInteriorPoint.getX, x._4.getInteriorPoint.getY)))
                val map = list.groupBy(_._1)
                for ((k, v) <- map) {
                    neigh(k) = v.collect { case (st, Some(en), Some(c), _) => (en, c) }
                    nodesToRegions(k) = region
                }
            }
        }

        override def singleRegion(rg: Int) = {
            val supGraph = super.singleRegion(rg)
            new GraphType[NodeType] {
                def nodes = {
                    ensureRegion(rg)
                    regionMap(rg)
                }

                def neighbours(nd: NodeType) = supGraph.neighbours(nd)
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

        def regionDiameter(rg: Int) = regionDiameters(rg)
    }

    type NodeType = Long

    type RegionType = Int
}

trait NodeCacheSizeParserComponent extends OptionParserComponent {
    lazy val nodeCacheSizeLens = registerConfig(500)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Int]("node-cache").action((x, c) => nodeCacheSizeLens.set(c)(x))
    }
}

trait ConfigDatabaseGraphComponent extends GraphWithRegionsComponent with DatabaseGraphComponent with NodeCacheSizeParserComponent with NodePositionComponent {
    self: RoadNetTableComponent with SessionProviderComponent with ArgumentParser =>

    val graph = new DatabaseGraph(roadNetTables, nodeCacheSizeLens.get(parsedConfig))
    val nodePos = graph
}

trait DefaultDatabaseGraphComponent extends GraphWithRegionsComponent with DatabaseGraphComponent with NodePositionComponent {
    self: RoadNetTableComponent with SessionProviderComponent =>
    val graph = new DatabaseGraph(roadNetTables, 500)
    val nodePos = graph
}

trait ConfigMultiLevelDatabaseGraph extends MultiLevelGraphComponent with GraphComponent with NodePositionComponent with DatabaseGraphComponent with NodeCacheSizeParserComponent {
    self: MultiLevelRoadNetTableComponent with SessionProviderComponent with ArgumentParser =>

    val levels = roadNetTableLevels.map(x => new DatabaseGraph(x, nodeCacheSizeLens.get(parsedConfig)))

    val graph = levels.head
    val nodePos = graph
}