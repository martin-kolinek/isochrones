package org.isochrone.areas

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.graphlib.GraphComponentBase

class AreaPropFinderTest extends FunSuite with TestDatabase {
    test("AreaPropertiesFinder works") {
        val comp = new TestDatabaseComponent with AreaPropertiesFinderComponent with DijkstraAlgorithmProviderComponent with DbAreaReaderComponent with SingleSessionProvider with RoadNetTableComponent with GraphComponentBase {
            override type NodeType = Long
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_")
            val reader = new DbAreaReader {}
        }

        comp.AreaPropertiesSaver.saveAreaProperties()
        comp.database.withTransaction { implicit s: Session =>
            val cst = comp.roadNetTables.roadAreas.filter(x => x.id === 1l && x.nodeId === 1l).map(_.costToCover).first
            assert(cst === 15)
            val cst2 = comp.roadNetTables.roadAreas.filter(x => x.id === 1l && x.nodeId === 2l).map(_.costToCover).first
            assert(cst2 === 15)
            val cst3 = comp.roadNetTables.roadAreas.filter(x => x.id === 1l && x.nodeId === 3l).map(_.costToCover).first
            assert(cst3 === 14)
            val cst4 = comp.roadNetTables.roadAreas.filter(x => x.id === 1l && x.nodeId === 4l).map(_.costToCover).first
            assert(cst4 === 15)

            assert(comp.roadNetTables.roadAreas.filter(_.id === 2l).sortBy(_.sequenceNo).map(_.costToCover).list === List(15.0, 14.0, 15.0, 15.0))

            assert(comp.roadNetTables.areaGeoms.list.size === 2)
        }

    }
}