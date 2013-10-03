package org.isochrone.db

import org.scalatest.FunSuite

class RegularPartitionTest extends FunSuite with TestDatabase {
    test("regular partition creates correct number of regions") {
        new RegularPartitionComponent with TestDatabaseComponent with RoadNetTableComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("incpart_")
            val regularPartition = new RegularPartition(1.0)

            assert(regularPartition.regions.size === 9)
        }
    }

    test("regular partition regions have correct size") {
        new RegularPartitionComponent with TestDatabaseComponent with RoadNetTableComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("incpart_")
            val regularPartition = new RegularPartition(1.0)

            assert(regularPartition.regions.forall(bbx => math.abs(bbx.right - bbx.left - 1.0) < 0.0001
                    && math.abs(bbx.top - bbx.bottom - 1.0) < 0.0001))
        }
    }
}