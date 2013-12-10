package org.isochrone.areas

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.graphlib.GraphComponentBase

class AreaPropFinderTest extends FunSuite with TestDatabase {
    test("AreaPropertiesFinder works") {
        val comp = new TestDatabaseComponent with AreaPropertiesFinderComponent with DefaultDijkstraProvider with DbAreaReaderComponent with SingleSessionProvider with RoadNetTableComponent with GraphComponentBase {
            override type NodeType = Long
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_")
            val reader = new DbAreaReader {}
        }

        comp.AreaPropertiesSaver.saveAreaProperties()
        comp.database.withTransaction { implicit s: Session =>
            val cst = comp.roadNetTables.roadAreas.filter(x => x.id === 1l && x.nodeId === 1l).map(_.costToCover).first
            assert(cst == 8)
            val cst2 = comp.roadNetTables.roadAreas.filter(x => x.id === 1l && x.nodeId === 2l).map(_.costToCover).first
            assert(cst2 == 8)
            val cst3 = comp.roadNetTables.roadAreas.filter(x => x.id === 1l && x.nodeId === 3l).map(_.costToCover).first
            assert(cst3 == 6)
            val cst4 = comp.roadNetTables.roadAreas.filter(x => x.id === 1l && x.nodeId === 4l).map(_.costToCover).first
            assert(cst4 == 8)

            assert(comp.roadNetTables.roadAreas.filter(_.id === 2l).sortBy(_.sequenceNo).map(_.costToCover).list == List(8, 6, 8, 8))

            assert(Query(comp.roadNetTables.areaGeoms).list.size == 2)
        }

    }
}