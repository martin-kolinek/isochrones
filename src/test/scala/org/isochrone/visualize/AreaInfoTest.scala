package org.isochrone.visualize

import org.scalatest.FunSuite
import spire.std.seq._
import spire.std.double._
import spire.syntax.normedVectorSpace._
import org.isochrone.util._
import org.isochrone.db.TestDatabase
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.scalatest.matchers.MustMatchers

class AreaInfoTest extends FunSuite with TestDatabase with MustMatchers {
    test("node areas work") {
        val comp = new DbAreaInfoComponent with RoadNetTableComponent with SingleSessionProvider with TestDatabaseComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_cach_")
        }

        assert(comp.areaInfoRetriever.getNodesAreas(List(2))(2).map(_.areaId).toSet === Set(1, 2))
        assert(comp.areaInfoRetriever.getNodesAreas(List(2))(2).map(_.costToCover).toSet === Set(10.0))
    }

    test("geometries work") {
        val comp = new DbAreaInfoComponent with RoadNetTableComponent with SingleSessionProvider with TestDatabaseComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_")
        }
        assert(comp.areaInfoRetriever.getAreaGeometries(Seq(1))(1).isValid)
    }

    test("pos areas work") {
        val comp = new DbAreaInfoComponent with RoadNetTableComponent with SingleSessionProvider with TestDatabaseComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_")
        }
        val area = comp.areaInfoRetriever.getAreas(Seq(1))(1)
        assert(area.area === comp.Area(List(1, 2, 3, 4)))
        assert(area.points.map(_.pos).exists(x => (x - vector(1.0, 2.0)).norm < 0.001))
        area.cost(3, 4) must be(5.0 plusOrMinus 0.0001)
    }
}