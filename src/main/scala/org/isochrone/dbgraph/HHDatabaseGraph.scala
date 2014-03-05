package org.isochrone.dbgraph

import org.isochrone.hh.HHTables
import org.isochrone.db.RoadNetTables
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.graphlib.GraphWithRegionsComponentBase
import org.isochrone.db.SessionProviderComponent
import org.isochrone.hh.NeighbourhoodSizes
import org.isochrone.hh.DescendLimitProvider
import org.isochrone.hh.ShortcutReverseLimitProvider

class HHDatabaseGraph(hhTables: HHTables, roadNetTables: RoadNetTables, maxRegions: Int, session: Session) extends DatabaseGraphBase(roadNetTables, maxRegions, session) with BasicDatabaseGraphFunctionality with NeighbourhoodSizes[Long] with DescendLimitProvider[Long] with ShortcutReverseLimitProvider[Long] {
    case class HHNodeProps(np: BasicNodeProps, dh: Double, descLimit: Double, shortcutRevLimit: Double)

    type QueryResult = (BasicQueryResult, Double, Option[Double], Option[Double])

    type QueryType = Query[((WrappedBasicQueryResult), Column[Double], Column[Option[Double]], Column[Option[Double]]), QueryResult]

    type NodeProperties = HHNodeProps

    override def extractBasicNodeProps(np: NodeProperties) = np.np

    def nodePropsFromQueryResult(qrs: List[QueryResult]) = {
        val basic = basicNodePropsFromQueryResult(qrs.map(_._1)).toMap
        qrs.map { x =>
            x._1._1 -> HHNodeProps(basic(x._1._1), x._2, x._3.getOrElse(Double.PositiveInfinity), x._4.getOrElse(Double.PositiveInfinity))
        }
    }

    def query(region: Int) = {
        val innerJoin = for {
            b <- basicQuery(region)
            hh <- hhTables.neighbourhoods if hh.nodeId === b._1
        } yield (b, hh)
        for {
            (((b, hh), desc), shortRev) <- innerJoin.
                leftJoin(hhTables.descendLimit).on(_._1._1 === _.nodeId).
                leftJoin(hhTables.shortcutReverseLimit).on(_._1._1._1 === _.nodeId)
        } yield (b, hh.neighbourhood, desc.descendLimit.?, shortRev.descendLimit.?)
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
}

trait HHDatabaseGraphComponent extends GraphWithRegionsComponentBase {
    self: SessionProviderComponent =>

    class SessionHHDatabaseGraph(hht: HHTables, rnt: RoadNetTables, cacheSize: Int) extends HHDatabaseGraph(hht, rnt, cacheSize, session)

    type NodeType = Long

    type RegionType = Int
}