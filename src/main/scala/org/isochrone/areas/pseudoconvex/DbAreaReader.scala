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

    class DbAreaReaderComponent extends AreaReader {
        def areas = {
            val q = for {
                n <- roadNetTables.roadNodes
                a <- roadNetTables.roadAreas if a.nodeId === n.id
            } yield (n, a)
            val iter = q.sortBy { case (n, a) => (a.id, a.sequenceNo) }.map {
                case (n, a) => (a.id, n.id, n.geom)
            }.elements()(session)
            for (l <- iter.partitionBy(_._1)) yield {
                val pts = l.map {
                    case (a, n, g) =>
                        new PointWithPosition(n, List(g.getInteriorPoint.getX, g.getInteriorPoint.getY))
                }
                Area(pts)
            }
        }
    }
}