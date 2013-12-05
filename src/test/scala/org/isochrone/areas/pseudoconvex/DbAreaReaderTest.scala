package org.isochrone.areas.pseudoconvex

import org.scalatest.FunSuite
import spire.std.double._
import spire.std.seq._
import spire.syntax.normedVectorSpace._
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.TestDatabase
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.areas.DbAreaReaderComponent

class DbAreaReaderTest extends FunSuite with TestDatabase {

    test("DbAreaReader reads areas") {
        new DbAreaReaderComponent with RoadNetTableComponent with SingleSessionProvider with TestDatabaseComponent with GraphComponentBase {
            val reader = new DbAreaReader {}
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("ar_")
            val lst = reader.areas.toList
            assert(lst.size == 2)
            val ar1 = lst(0)
            assert(ar1.points.map(_.nd) == List(1, 2, 3, 4))
            assert(math.abs(ar1.cost(2, 1) - 2) < 0.001)
            assert(math.abs(ar1.cost(1, 2) - 1) < 0.001)
            assert((ar1.points(0).pos - List(2.0, 2)).norm < 0.001)
            assert((ar1.points(1).pos - List(2.0, 1)).norm < 0.001)
            assert(lst(1).points.map(_.nd) == List(2, 3, 4, 1))
        }
    }

}