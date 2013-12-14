package org.isochrone.visualize

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix

class AreaCacheTest extends FunSuite with TestDatabase {
    test("AreaCache works") {
        val comp = new AreaCacheComponent with RoadNetTableComponent with SingleSessionProvider with TestDatabaseComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_cach_")
            val areaCache = new AreaCache(4)
        }

        assert(comp.areaCache.getNodeAreas(2).map(_.areaId).toSet == Set(1, 2))
        assert(comp.areaCache.rememberedNodes == 5)
        assert(comp.areaCache.getNodeAreas(2).map(_.costToCover).toSet == Set(10.0))
    }
}