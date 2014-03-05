package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.db.EdgeTable

class TreeContractionTest extends FunSuite with TestDatabase {
    test("Tree contraction works") {
        new TestDatabaseComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("tree_")
            val output = TableQuery(t => new EdgeTable(t, "tree_road_net_out"))
            database.withTransaction { implicit s: Session =>
                TreeContraction.contractTrees(roadNetTables, output, s)
                val got = output.map(x => (x.start, x.end, x.cost)).buildColl[Set]
                assert(got === Set((4, 3, 1.0), (5, 3, 2.0), (6, 3, 3.0), (7, 3, 3.0)))
                val got2 = roadNetTables.roadNet.map(x => x.start -> x.end).buildColl[Set]
                assert(got2 === Set(1 -> 2, 2 -> 1, 1 -> 3, 3 -> 1, 2 -> 3, 3 -> 2))
            }
        }
    }
}