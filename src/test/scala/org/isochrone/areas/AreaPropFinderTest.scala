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
import org.isochrone.osm.SpeedCostAssignerComponent

class AreaPropFinderTest extends FunSuite with TestDatabase {
    test("AreaPropertiesFinder works") {
        val comp = new TestDatabaseComponent with AreaPropertiesFinderComponent with DijkstraAlgorithmProviderComponent with DbAreaReaderComponent with SingleSessionProvider with RoadNetTableComponent with GraphComponentBase with SpeedCostAssignerComponent {
            def noRoadSpeed = 1
            def roadSpeed = 1000
            override type NodeType = Long
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_")
            val reader = new DbAreaReader {}
        }

        comp.AreaPropertiesSaver.saveAreaProperties()
        comp.database.withTransaction { implicit s: Session =>
            val lst = comp.roadNetTables.roadAreas.map(_.costToCover).list
            info(lst.toString)
            lst.foreach { cst =>
                assert(cst >= 75 && cst <= 120)
            }

            assert(comp.roadNetTables.roadAreas.list.size === 8)

            assert(comp.roadNetTables.areaGeoms.list.size === 2)
        }

    }
}