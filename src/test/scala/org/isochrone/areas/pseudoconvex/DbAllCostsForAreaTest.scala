package org.isochrone.areas.pseudoconvex

import org.scalatest.FunSuite
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.TestDatabase
import org.isochrone.osm.CostAssignerComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.db.DefaultRoadNetTablesWithPrefix

class DbAllCostsForAreaTest extends FunSuite with TestDatabase {
    test("DbAllCostsForArea works") {
        val comp = new DbAllCostsForAreaComponent with RoadNetTableComponent with SingleSessionProvider with TestDatabaseComponent with CostAssignerComponent {
            def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = 0.0
            def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = 10.0
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_")
        }
        val costs = comp.allCostsForArea(comp.Area(1, Nil, Map()))
        info(costs.toString)
        assert(costs.size == 6)
        
    }
}