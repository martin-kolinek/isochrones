package org.isochrone.areas.pseudoconvex

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.TestDatabase
import org.isochrone.osm.CostAssignerComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.db.DefaultRoadNetTablesWithPrefix

class EdgeCostResolverTest extends FunSuite with TestDatabase {
    test("database edge cost resolver works") {
        new DbEdgeCostResolverComponent with TestDatabaseComponent with CostAssignerComponent with GraphComponentBase with RoadNetTableComponent {
            type NodeType = Long
            def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = c1.distanceSphere(c2).asColumnOf[Double] / 10.0
            def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = c1.distanceSphere(c2).asColumnOf[Double]
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("test_")

            val res = resolver.resolve(List((5000000000l, 5000000001l))).toList
            assert(res.size == 1)
            assert(math.abs(res(0).cost - 11119.5079734632) < 0.0001)
            assert(res(0).nds == Set(5000000000l, 5000000001l))
        }
    }
}