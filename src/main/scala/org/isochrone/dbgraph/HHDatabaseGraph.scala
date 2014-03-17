package org.isochrone.dbgraph

import org.isochrone.hh.HHTables
import org.isochrone.db.RoadNetTables
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.graphlib.GraphWithRegionsComponentBase
import org.isochrone.db.SessionProviderComponent
import org.isochrone.hh.NeighbourhoodSizes
import org.isochrone.hh.DescendLimitProvider
import org.isochrone.hh.ShortcutReverseLimitProvider
import org.isochrone.db.BasicRoadNetTables
import org.isochrone.hh.MultiLevelHHTableComponent
import org.isochrone.db.MultiLevelRoadNetTableComponent
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.graphlib.GraphComponentBaseWithDefault
import org.isochrone.graphlib.GraphComponent
import org.isochrone.db.NodeTable
import org.isochrone.hh.HHProps

class HHDatabaseGraph(hhTables: HHTables, roadNetTables: BasicRoadNetTables, higherNodeTable: Option[TableQuery[NodeTable]], maxRegions: Int, session: Session)
        extends DatabaseGraphBase(roadNetTables, maxRegions, session)
        with BasicDatabaseGraphFunctionality
        with HHProps[Long] {
    case class HHNodeProps(np: BasicNodeProps, dh: Double, descLimit: Double, shortcutRevLimit: Double, reverseNeighSize: Double, hasHigher: Boolean)

    type QueryResult = (BasicQueryResult, Option[Double], Option[Double], Option[Double], Option[Double], Option[Long])

    type QueryType = Query[((WrappedBasicQueryResult), Column[Option[Double]], Column[Option[Double]], Column[Option[Double]], Column[Option[Double]], Column[Option[Long]]), QueryResult]

    type NodeProperties = HHNodeProps

    override def extractBasicNodeProps(np: NodeProperties) = np.np

    def nodePropsFromQueryResult(qrs: List[QueryResult]) = {
        val basic = basicNodePropsFromQueryResult(qrs.map(_._1)).toMap
        qrs.map { x =>
            x._1._1 -> HHNodeProps(basic(x._1._1), x._2.getOrElse(Double.PositiveInfinity), x._3.getOrElse(Double.PositiveInfinity), x._4.getOrElse(Double.PositiveInfinity), x._5.getOrElse(Double.PositiveInfinity), x._6.nonEmpty)
        }
    }

    def query(region: Int) = {
        val withoutHigher: QueryType = for {
            ((((b, hh), desc), shortRev), revNeigh) <- basicQuery(region).
                leftJoin(hhTables.neighbourhoods).on(_._1 === _.nodeId).
                leftJoin(hhTables.descendLimit).on(_._1._1 === _.nodeId).
                leftJoin(hhTables.shortcutReverseLimit).on(_._1._1._1 === _.nodeId).
                leftJoin(hhTables.reverseNeighbourhoods).on(_._1._1._1._1 === _.nodeId)
        } yield (b, hh.neighbourhood.?, desc.descendLimit.?, shortRev.descendLimit.?, revNeigh.neighbourhood.?, None)

        (withoutHigher /: higherNodeTable) { (wh, higherNd) =>
            for ((x, y) <- wh.leftJoin(higherNd).on(_._1._1 === _.id))
                yield (x._1, x._2, x._3, x._4, x._5, y.id.?)
        }
    }

    def neighbourhoodSize(nd: Long) = {
        propsForNode(nd).dh
    }

    def descendLimit(nd: Long) = {
        propsForNode(nd).descLimit
    }

    def shortcutReverseLimit(nd: Long) = {
        propsForNode(nd).shortcutRevLimit
    }

    def hasHigherLevel(nd: Long) = {
        propsForNode(nd).hasHigher
    }

    def reverseNeighSize(nd: Long) = {
        propsForNode(nd).reverseNeighSize
    }

    override def toString = s"HHDatabaseGraph($roadNetTables)"
}

trait HHDatabaseGraphComponent extends GraphWithRegionsComponentBase {
    self: SessionProviderComponent =>

    class SessionHHDatabaseGraph(hht: HHTables, rnt: RoadNetTables, higherNodeTable: Option[TableQuery[NodeTable]], cacheSize: Int) extends HHDatabaseGraph(hht, rnt, higherNodeTable, cacheSize, session)

    type NodeType = Long

    type RegionType = Int
}

trait MultiLevelHHDatabaseGraphComponent extends DBGraphConfigParserComponent with NodePositionComponent with GraphComponent {
    self: MultiLevelHHTableComponent with SessionProviderComponent with MultiLevelRoadNetTableComponent with ArgumentParser =>

    type NodeType = Long

    private val graphConfig = dbGraphConfLens.get(parsedConfig)

    private val rnwithhh = roadNetTableLevels zip hhTableLevels

    val hhDbGraphs = {
        for {
            ((rn, hh), higherrn) <- rnwithhh zip (roadNetTableLevels.drop(1).map(x => Some(x)) :+ None)
        } yield new HHDatabaseGraph(hh, rn, higherrn.map(_.roadNodes), graphConfig.effectiveNodeCacheSize, session)
    }.toIndexedSeq

    private val shortcutRoadNets = for ((rn, hh) <- rnwithhh) yield new BasicRoadNetTables {
        val roadNet = hh.shortcutEdges
        val roadNodes = rn.roadNodes
        val roadRegions = rn.roadRegions
    }

    private val revShortcutRoadNets = for ((rn, hh) <- rnwithhh) yield new BasicRoadNetTables {
        val roadNet = hh.reverseShortcutEdges
        val roadNodes = rn.roadNodes
        val roadRegions = rn.roadRegions
    }

    val shortcutGraphs = shortcutRoadNets.map { rn =>
        new DatabaseGraph(rn, graphConfig.effectiveNodeCacheSize, session)
    }.toIndexedSeq

    val reverseShortcutGraph = revShortcutRoadNets.map { rn =>
        new DatabaseGraph(rn, graphConfig.effectiveNodeCacheSize, session)
    }.toIndexedSeq

    val graph = hhDbGraphs.head

    val nodePos = graph
}