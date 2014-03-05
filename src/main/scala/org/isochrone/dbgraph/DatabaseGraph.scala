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
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.graphlib.MapGraphType
import org.isochrone.db.EdgeTable

trait BasicDatabaseGraphFunctionality extends GraphWithRegionsType[Long, Int] with MapGraphType[Long] with NodePosition[Long] {
    self: DatabaseGraphBase =>

    final type BasicQueryType = Query[WrappedBasicQueryResult, BasicQueryResult]

    final type WrappedBasicQueryResult = (Column[Long], Column[Option[Long]], Column[Option[Double]], Column[Geometry])

    final type BasicQueryResult = (Long, Option[Long], Option[Double], Geometry)

    protected case class BasicNodeProps(neighs: Map[Long, Double], pos: (Double, Double))

    def edgeStartSelector(tbl: EdgeTable) = tbl.start
    def edgeEndSelector(tbl: EdgeTable) = tbl.end

    def basicQuery(region: Int): BasicQueryType = {
        val startJoin = roadNetTables.roadNodes leftJoin roadNetTables.roadNet on ((n, e) => n.id === edgeStartSelector(e))
        for ((n, e) <- startJoin if n.region === region) yield (n.id, edgeEndSelector(e).?, e.cost.?, n.geom)
    }

    def basicNodePropsFromQueryResult(qrs: List[BasicQueryResult]): Traversable[(Long, BasicNodeProps)] = {
        for ((n, lst) <- qrs.groupBy(_._1).view) yield {
            val neighs = lst.collect { case (st, Some(en), Some(c), _) => (en, c) }.toMap
            val pos = (lst.head._4.getInteriorPoint.getX, lst.head._4.getInteriorPoint.getY)
            n -> BasicNodeProps(neighs, pos)
        }
    }

    def nodePosition(nd: Long) = {
        extractBasicNodeProps(propsForNode(nd)).pos
    }

    def neighbours(node: Long) = {
        extractBasicNodeProps(propsForNode(node)).neighs
    }

    def extractBasicNodeProps(np: NodeProperties): BasicNodeProps

    override def singleRegion(rg: Int) = {
        val supGraph = super.singleRegion(rg)
        new GraphType[Long] {
            def nodes = {
                ensureRegion(rg)
                regionMap(rg)
            }

            def neighbours(nd: Long) = supGraph.neighbours(nd)

            def edgeCost(start: Long, end: Long) = supGraph.edgeCost(start, end)
        }
    }

    def regions = regionDiameters.map(_._1)

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

    def nodes = roadNetTables.roadNodes.map(_.id).list()(session)
}

trait ReverseDatabaseGraphFunctionality extends BasicDatabaseGraphFunctionality {
    self: DatabaseGraphBase =>

    override def edgeStartSelector(tbl: EdgeTable) = tbl.end
    override def edgeEndSelector(tbl: EdgeTable) = tbl.start
}

class DatabaseGraphWithoutFunc(roadNetTables: RoadNetTables, maxRegions: Int, session: Session) extends DatabaseGraphBase(roadNetTables, maxRegions, session) {
    self: BasicDatabaseGraphFunctionality =>
    def extractBasicNodeProps(np: NodeProperties) = np

    type NodeProperties = BasicNodeProps

    type QueryType = BasicQueryType

    type QueryResult = BasicQueryResult

    def nodePropsFromQueryResult(qrs: List[QueryResult]): Traversable[(Long, NodeProperties)] = basicNodePropsFromQueryResult(qrs)

    def query(region: Int): QueryType = basicQuery(region)
}

class DatabaseGraph(roadNetTables: RoadNetTables, maxRegions: Int, session: Session) extends DatabaseGraphWithoutFunc(roadNetTables, maxRegions, session) with BasicDatabaseGraphFunctionality

class ReverseDatabaseGraph(roadNetTables: RoadNetTables, maxRegions: Int, session: Session) extends DatabaseGraphWithoutFunc(roadNetTables, maxRegions, session) with ReverseDatabaseGraphFunctionality

trait DBGraphConfigParserComponent extends OptionParserComponent with Logging {
    case class DBGraphConfig(preloadAll: Boolean, nodeCacheSize: Int) {
        def effectiveNodeCacheSize = if (preloadAll) Int.MaxValue else nodeCacheSize
        def preload(g: BasicDatabaseGraphFunctionality with DatabaseGraphBase) {
            if (preloadAll) {
                logger.info("Preloading graph")
                g.regions.foreach(g.ensureRegion)
                logger.info("Done preloading graph")
            }
        }
    }

    lazy val dbGraphConfLens = registerConfig(DBGraphConfig(false, 500))

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Int]("node-cache").action((x, c) => dbGraphConfLens.modify(c)(_.copy(nodeCacheSize = x)))
        pars.opt[Boolean]("preload-all").text("whether to preload whole database graph into memory").
            action((x, c) => dbGraphConfLens.modify(c)(_.copy(preloadAll = x)))
    }
}

trait ConfigDatabaseGraphComponent extends GraphWithRegionsComponent with DBGraphConfigParserComponent with NodePositionComponent {
    self: RoadNetTableComponent with SessionProviderComponent with ArgumentParser =>
    type NodeType = Long
    type RegionType = Int

    val graph = {
        val graphConfig = dbGraphConfLens.get(parsedConfig)
        val ret = new DatabaseGraph(roadNetTables, graphConfig.effectiveNodeCacheSize, session)
        graphConfig.preload(ret)
        ret
    }

    val nodePos = graph
}

trait DefaultDatabaseGraphComponent extends GraphWithRegionsComponent with NodePositionComponent {
    self: RoadNetTableComponent with SessionProviderComponent =>
    type NodeType = Long
    type RegionType = Int
    val graph = new DatabaseGraph(roadNetTables, 500, session)
    val nodePos = graph
}

trait ConfigMultiLevelDatabaseGraph extends MultiLevelGraphComponent with GraphComponent with NodePositionComponent with DBGraphConfigParserComponent {
    self: MultiLevelRoadNetTableComponent with SessionProviderComponent with ArgumentParser =>
    type NodeType = Long
    type RegionType = Int

    val levels = {
        val graphConf = dbGraphConfLens.get(parsedConfig)
        val ret = roadNetTableLevels.map(x => new DatabaseGraph(x, graphConf.effectiveNodeCacheSize, session))
        ret.foreach(graphConf.preload)
        ret
    }

    val graph = levels.head
    val nodePos = graph
}