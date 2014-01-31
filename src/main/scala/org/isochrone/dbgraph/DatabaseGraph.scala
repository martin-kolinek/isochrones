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

trait BasicDatabaseGraphFunctionality extends GraphWithRegionsType[Long, Int] with NodePosition[Long] {
    self: DatabaseGraphBase =>

    final type BasicQueryType = Query[WrappedBasicQueryResult, BasicQueryResult]

    final type WrappedBasicQueryResult = (Column[Long], Column[Option[Long]], Column[Option[Double]], Column[Geometry])

    final type BasicQueryResult = (Long, Option[Long], Option[Double], Geometry)

    protected case class BasicNodeProps(neighs: Traversable[(Long, Double)], pos: (Double, Double))

    final def basicQuery(region: Int): BasicQueryType = {
        val startJoin = roadNetTables.roadNodes leftJoin roadNetTables.roadNet on ((n, e) => n.id === e.start)
        for ((n, e) <- startJoin.sortBy(x => (x._1.id, x._2.end)) if n.region === region) yield (n.id, e.end.?, e.cost.?, n.geom)
    }

    final def basicNodePropsFromQueryResult(qrs: List[BasicQueryResult]): Traversable[(Long, BasicNodeProps)] = {
        for ((n, lst) <- qrs.groupBy(_._1).view) yield {
            val neighs = lst.collect { case (st, Some(en), Some(c), _) => (en, c) }
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

class DatabaseGraph(roadNetTables: RoadNetTables, maxRegions: Int, session: Session) extends DatabaseGraphBase(roadNetTables, maxRegions, session) with BasicDatabaseGraphFunctionality {

    def extractBasicNodeProps(np: NodeProperties) = np

    type NodeProperties = BasicNodeProps

    type QueryType = BasicQueryType

    type QueryResult = BasicQueryResult

    def nodePropsFromQueryResult(qrs: List[QueryResult]): Traversable[(Long, NodeProperties)] = basicNodePropsFromQueryResult(qrs)

    def query(region: Int): QueryType = basicQuery(region)
}

trait DatabaseGraphComponent extends GraphWithRegionsComponentBase {
    self: SessionProviderComponent =>

    class SessionDatabaseGraph(rnt: RoadNetTables, cacheSize: Int) extends DatabaseGraph(rnt, cacheSize, session)

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

    val graph = new SessionDatabaseGraph(roadNetTables, nodeCacheSizeLens.get(parsedConfig))
    val nodePos = graph
}

trait DefaultDatabaseGraphComponent extends GraphWithRegionsComponent with DatabaseGraphComponent with NodePositionComponent {
    self: RoadNetTableComponent with SessionProviderComponent =>
    val graph = new SessionDatabaseGraph(roadNetTables, 500)
    val nodePos = graph
}

trait ConfigMultiLevelDatabaseGraph extends MultiLevelGraphComponent with GraphComponent with NodePositionComponent with DatabaseGraphComponent with NodeCacheSizeParserComponent {
    self: MultiLevelRoadNetTableComponent with SessionProviderComponent with ArgumentParser =>

    val levels = roadNetTableLevels.map(x => new SessionDatabaseGraph(x, nodeCacheSizeLens.get(parsedConfig)))

    val graph = levels.head
    val nodePos = graph
}