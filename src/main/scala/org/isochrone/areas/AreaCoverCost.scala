package org.isochrone.areas

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.osm.SpeedCostAssignerComponent

trait AreaCoverCostComponent extends AreaGraphComponent {
    self: DijkstraAlgorithmProviderComponent with GraphComponentBase with SpeedCostAssignerComponent =>

    object AreaCoverCostDeterminer {
        def getCostsForArea(ar: PosArea): Traversable[(NodeType, Double)] = {
            val grp = AreaWithDiagonalsGraph(ar, Nil)
            val dijk = dijkstraForGraph(grp)
            val posMap = ar.points.map(x => x.nd -> x.pos).toMap

            for (PointWithPosition(nd, pos) <- ar.points) yield {
                val costs = for ((other, ocost) <- dijk.helper.compute(nd)) yield {
                    val distToOther = math.max(getNoDbNoRoadCost(pos, posMap(other)), ar.costs.get(nd -> other).getOrElse(0.0))
                    (distToOther / 2.0) + (ocost / 2.0)
                }
                nd -> costs.max
            }
        }
    }
}
