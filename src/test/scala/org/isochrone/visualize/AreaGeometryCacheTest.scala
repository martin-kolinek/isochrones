package org.isochrone.visualize

import org.scalatest.FunSuite
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.TestDatabase
import org.isochrone.db.DefaultRoadNetTablesWithPrefix

class AreaGeometryCacheTest extends FunSuite with TestDatabase {
    test("AreaGeometryCache works") {
        val comp = new DbAreaGeometryCacheComponent with RoadNetTableComponent with SingleSessionProvider with TestDatabaseComponent {
            val areaGeomCache = new DbAreaGeometryCache(10)
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_")
        }
        assert(comp.areaGeomCache.getAreaGeom(1).isValid)
    }
}