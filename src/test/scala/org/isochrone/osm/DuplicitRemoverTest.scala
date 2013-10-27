package org.isochrone.osm

import org.scalatest.FunSuite
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.util.db.MyPostgresDriver.simple._

class DuplicitRemoverTest extends FunSuite with TestDatabase {
    trait TestDupRem extends DuplicitRemoverComponent with RoadNetTableComponent with TestDatabaseComponent {
        val roadNetTables = new DefaultRoadNetTablesWithPrefix("dup_")
    }

    test("removing duplicate nodes works") {
        val comp = new TestDupRem {}
        comp.DuplicitRemover.removeDupNodes()
        comp.database.withTransaction { implicit s: Session =>
            val lst = Query(comp.roadNetTables.roadNodes).map(_.id).list.sorted
            info(lst.toString)
            assert(lst == List(1, 3))
            val rs = Query(comp.roadNetTables.roadNet).map(x => (x.start, x.end)).list
            assert(!rs.map(_._1).contains(2))
            assert(!rs.map(_._1).contains(4))
            assert(!rs.map(_._2).contains(2))
            assert(!rs.map(_._2).contains(4))
        }
    }

    test("removing duplicate edges works") {
        val comp = new TestDupRem {}
        comp.DuplicitRemover.removeDupEdges()
        comp.DuplicitRemover.removeDupNodes()
        comp.database.withTransaction { implicit s: Session =>
            val lst = Query(comp.roadNetTables.roadNet).list
            info(lst.toString)
            assert(lst.size == 1)
        }
    }
}