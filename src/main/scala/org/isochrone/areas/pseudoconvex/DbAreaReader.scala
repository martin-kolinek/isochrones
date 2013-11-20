package org.isochrone.areas.pseudoconvex

import org.isochrone.util.db.MyPostgresDriver.simple._
import slick.jdbc.StaticQuery.interpolation
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.util._
import org.isochrone.db.SessionProviderComponent

trait DbAreaReaderComponent extends AreaReaderComponent {
    self: RoadNetTableComponent with GraphComponentBase with SessionProviderComponent =>

    type NodeType = Long

    class DbAreaReader extends AreaReader {
        def areas = {
            val q = for {
                n <- roadNetTables.roadNodes
                a <- roadNetTables.roadAreas if a.nodeId === n.id
            } yield (n, a)
            val costQ = for {
                a1 <- roadNetTables.roadAreas
                a2 <- roadNetTables.roadAreas if a1.id === a2.id && a1.sequenceNo === a2.sequenceNo - 1
                rn <- roadNetTables.roadNet if rn.start === a1.nodeId && rn.end === a2.nodeId
                rn2 <- roadNetTables.roadNet if rn2.end === a1.nodeId && rn2.start === a2.nodeId
            } yield (a1.id, a1.nodeId, a2.nodeId, rn.cost, rn2.cost)
            val finalCostQ = for {
                a1 <- roadNetTables.roadAreas if a1.sequenceNo === 0
                a2 <- roadNetTables.roadAreas if a2.id === a1.id
                if !Query(roadNetTables.roadAreas).filter(_.id === a1.id).filter(_.sequenceNo > a2.sequenceNo).map(_.sequenceNo).exists
                rn <- roadNetTables.roadNet if rn.start === a1.nodeId && rn.end === a2.nodeId
                rn2 <- roadNetTables.roadNet if rn2.end === a1.nodeId && rn2.start === a2.nodeId
            } yield (a1.id, a1.nodeId, a2.nodeId, rn.cost, rn2.cost)

            val iter = q.sortBy { case (n, a) => (a.id, a.sequenceNo) }.map {
                case (n, a) => (a.id, n.id, n.geom)
            }.elements()(session)
            val costIter = costQ.sortBy(_._1).elements()(session)
            val finalCostIter = finalCostQ.sortBy(_._1).elements()(session)

            for (((l, costs), finCost) <- iter.partitionBy(_._1) zip costIter.partitionBy(_._1) zip finalCostIter) yield {
                val pts = l.map {
                    case (a, n, g) =>
                        new PointWithPosition(n, List(g.getInteriorPoint.getX, g.getInteriorPoint.getY))
                }
                val csts = (costs :+ finCost).flatMap {
                    case (_, n1, n2, c1, c2) => List((n1, n2) -> c1, (n2, n1) -> c2)
                }.toMap
                Area(pts, csts)
            }
        }
    }
}