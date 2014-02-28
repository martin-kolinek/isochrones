package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.util.db.MyPostgresDriver.simple._

class AreaDescendLimitFinderTest extends FunSuite with TestDatabase {
    test("AreaDescendLimitFinder works") {
        new TestDatabaseComponent with AreaDescendLimitFinderComponent with RoadNetTableComponent with HHTableComponent {
            val hhTables = new DefaultHHTablesWithPrefix("hhar_")
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("hhar_")
            AreaDescendLimitFinder.execute()
            database.withSession { implicit s: Session =>
                val lst = Query(hhTables.descendLimit).list
                info(lst.toString)
                assert(lst.size === 5)
                assert(lst.toSet === Set(1 -> 3, 2 -> 2, 3 -> 1.5, 4 -> 3.5, 5 -> 4.5))
            }
        }
    }
}