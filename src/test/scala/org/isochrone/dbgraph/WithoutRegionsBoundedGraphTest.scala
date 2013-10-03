package org.isochrone.dbgraph

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.TestDatabase
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix

class WithoutRegionsBoundedGraphTest extends FunSuite with TestDatabase {
    test("WithoutRegionsBoundedGraph has required nodes") {
        new WithoutRegionsBoundedGraphComponent with TestDatabaseComponent with RoadNetTableComponent with RegularPartitionComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("incpart_")
            val regularPartition = new RegularPartition(1)
            val first = regularPartition.regions.head
            val grph = WithoutRegionsBoundedGraphCreator.createGraph(first)
            val x = grph.nodes.toSet
            assert(grph.nodes.toSet == Set(1l, 10l))
        }
    }
}