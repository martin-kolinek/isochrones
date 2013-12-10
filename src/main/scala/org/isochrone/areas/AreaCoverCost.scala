package org.isochrone.areas

import org.isochrone.dijkstra.DijkstraProvider
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.typesafe.scalalogging.slf4j.Logging

trait AreaCoverCostComponent extends AreaGraphComponent {
    self: DijkstraProvider with GraphComponentBase =>

    object AreaCoverCostDeterminer {
        def getCostsForArea(ar: PosArea): Traversable[(NodeType, Double)] = {
            val grp = AreaWithDiagonalsGraph(ar, Nil)
            val dijk = dijkstraForGraph(grp)
            for (PointWithPosition(nd, _) <- ar.points) yield {
                nd -> dijk.DijkstraHelpers.compute(nd).map(_._2).max
            }
        }
    }
}
