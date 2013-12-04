package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.SessionProviderComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.osm.CostAssignerComponent

trait AllCostsForAreaComponent extends PosAreaComponent {
    self: GraphComponentBase =>
    def allCostsForArea(ar: Area): Traversable[EdgeWithCost]
}

trait DbAllCostsForAreaComponent extends AllCostsForAreaComponent with GraphComponentBase {
    self: RoadNetTableComponent with SessionProviderComponent with CostAssignerComponent =>
    type NodeType = Long

    def allCostsForArea(ar: Area) = {
        val q = for {
            ar1 <- roadNetTables.roadAreas
            ar2 <- roadNetTables.roadAreas if ar1.id === ar2.id && ar1.nodeId < ar2.nodeId
            n1 <- roadNetTables.roadNodes if n1.id === ar1.nodeId
            n2 <- roadNetTables.roadNodes if n2.id === ar2.nodeId
            if ar1.id === ar.id
        } yield (n1.id, n2.id, getNoRoadCost(n1.geom, n2.geom))
        q.list()(session).map {
            case (a, b, cost) => EdgeWithCost(Set(a, b), cost)
        }
    }
}