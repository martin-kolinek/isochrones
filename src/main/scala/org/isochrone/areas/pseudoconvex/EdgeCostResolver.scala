package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.osm.CostAssignerComponent

trait EdgeCostResolverComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    trait EdgeCostResolver {
        def resolve(edgs: Traversable[(NodeType, NodeType)]): Traversable[EdgeWithCost]
    }
    val resolver: EdgeCostResolver
}

trait DbEdgeCostResolverComponent extends EdgeCostResolverComponent with GraphComponentBase {
    self: DatabaseProvider with RoadNetTableComponent with CostAssignerComponent =>

    type NodeType <: Long

    object DbEdgeCostResolver extends EdgeCostResolver {
        def resolve(edgs: Traversable[(NodeType, NodeType)]) = {
            val q = for {
                (a, b) <- Parameters[(Long, Long)]
                n1 <- roadNetTables.roadNodes if n1.id === a
                n2 <- roadNetTables.roadNodes if n2.id === b
            } yield getNoRoadCost(n1.geom, n2.geom)
            database.withSession { implicit s: Session =>
                for ((a, b) <- edgs.toList)
                    yield EdgeWithCost(Set(a, b), q(a -> b).first)
            }
        }
    }

    val resolver = DbEdgeCostResolver
}