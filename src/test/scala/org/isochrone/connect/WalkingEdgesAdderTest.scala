package org.isochrone.connect

import org.scalatest.FunSuite
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.dbgraph.DatabaseGraphComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.graphlib.GraphComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.TestDatabase
import org.isochrone.osm.SpeedCostAssignerComponent
import org.isochrone.dbgraph.DefaultDatabaseGraphComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.util.db.MyPostgresDriver.simple._

class WalkingEdgesAdderTest extends FunSuite with TestDatabase {
    trait WalkRoadNetTableComponent extends RoadNetTableComponent {
        val roadNetTables = new DefaultRoadNetTablesWithPrefix("walk_")
    }
    test("SimpleWalkingEdgesAdder works") {
        val comp = new SimpleWalkingEdgesAdderComponent with WalkRoadNetTableComponent with DijkstraAlgorithmComponent with SingleSessionProvider with TestDatabaseComponent with SpeedCostAssignerComponent with MaxCostQuotientComponent with DefaultDatabaseGraphComponent {
            override type NodeType = Long
            def maxCostQuotient = 1
            def maxDistanceQuotient = 1
            def noRoadSpeed = 1.0 / 1000.0
            def roadSpeed = 2.0 / 1000.0
        }

        comp.addWalkingEdges()
        comp.database.withTransaction { implicit s: Session =>
            val rn = comp.roadNetTables.roadNet
            val lst = Query(rn).list
            info(lst.toString)
            assert(lst.size == 3)
            val newEdge = lst.find(x => x._1 == 1 && x._2 == 3)
            assert(newEdge.isDefined)
            assert(math.abs(newEdge.get._3 - 11119) < 1)
            assert(newEdge.get._4)
            assert(newEdge.get._5.isValid)
        }
    }
}