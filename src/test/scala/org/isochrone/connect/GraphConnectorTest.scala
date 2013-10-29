package org.isochrone.connect

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.HigherLevelRoadNetTableComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.LineString
import org.isochrone.osm.CostAssignerComponent
import com.vividsolutions.jts.geom.Geometry

class GraphConnectorTest extends FunSuite with TestDatabase {
    test("Graph connecting works") {
        val comp = new GraphConnectorComponent with TestDatabaseComponent with RoadNetTableComponent with HigherLevelRoadNetTableComponent with CostAssignerComponent {
            def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = 10.0
            def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = 3.0

            val roadNetTables = new DefaultRoadNetTablesWithPrefix("con_")
            val higherRoadNetTables = new DefaultRoadNetTablesWithPrefix("con_higher_")
        }

        comp.GraphConnector.connectGraph()

        comp.database.withSession { implicit s: Session =>
            val lst = comp.roadNetTables.roadNet.filter(_.start === 3l).filter(_.end === 1l).map(x => (x.cost, x.virtual, x.geom)).list
            assert(lst.size == 1)
            assert(lst(0)._1 === 3.0)
            assert(lst(0)._2 == true)
            assert(lst(0)._3.isInstanceOf[LineString])
            val lst2 = comp.roadNetTables.roadNet.filter(_.start === 1l).filter(_.end === 3l).map(x => (x.cost, x.virtual, x.geom)).list
            assert(lst2.size == 1)
            assert(lst2(0)._1 === 3.0)
            assert(lst2(0)._2 == true)
            assert(lst2(0)._3.isInstanceOf[LineString])
        }
    }
}