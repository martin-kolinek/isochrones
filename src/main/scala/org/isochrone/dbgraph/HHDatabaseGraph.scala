package org.isochrone.dbgraph

import org.isochrone.hh.HHTables
import org.isochrone.db.RoadNetTables
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.graphlib.GraphWithRegionsComponentBase
import org.isochrone.db.SessionProviderComponent

class HHDatabaseGraph(hhTables: HHTables, roadNetTables: RoadNetTables, maxRegions: Int, session: Session) extends DatabaseGraphBase(roadNetTables, maxRegions, session) with BasicDatabaseGraphFunctionality {
    case class HHNodeProps(np: BasicNodeProps, dh: Double)

    type QueryResult = (BasicQueryResult, Double)

    type QueryType = Query[((WrappedBasicQueryResult), Column[Double]), QueryResult]

    type NodeProperties = HHNodeProps

    override def extractBasicNodeProps(np: NodeProperties) = np.np

    def nodePropsFromQueryResult(qrs: List[QueryResult]) = {
        val basic = basicNodePropsFromQueryResult(qrs.map(_._1)).toMap
        qrs.map { x =>
            x._1._1 -> HHNodeProps(basic(x._1._1), x._2)
        }
    }

    def query(region: Int) = {
        for {
            b <- basicQuery(region)
            hh <- hhTables.neighbourhoods if hh.nodeId === b._1
        } yield (b, hh.neighbourhood)
    }

    def neighbourhoodSize(nd: Long) = {
        propsForNode(nd).dh
    }
}

trait HHDatabaseGraphComponent extends GraphWithRegionsComponentBase {
    self: SessionProviderComponent =>

    class SessionHHDatabaseGraph(hht: HHTables, rnt: RoadNetTables, cacheSize: Int) extends HHDatabaseGraph(hht, rnt, cacheSize, session)

    type NodeType = Long

    type RegionType = Int
}