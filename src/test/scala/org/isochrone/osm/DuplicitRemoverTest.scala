package org.isochrone.osm

import org.scalatest.FunSuite
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.VisualizationTableComponent
import org.isochrone.db.DefaultVisualizationTableComponent

class DuplicitRemoverTest extends FunSuite with TestDatabase {
    trait TestDupRem extends DuplicitRemoverComponent with RoadNetTableComponent with TestDatabaseComponent {
        val roadNetTables = new DefaultRoadNetTablesWithPrefix("dup_")
    }

    test("removing duplicate edges works") {
        val comp = new TestDupRem {}
        comp.DuplicitRemover.removeDuplicates()

        comp.database.withTransaction { implicit s: Session =>
            val lst = comp.roadNetTables.roadNodes.map(_.id).list.sorted
            info(lst.toString)
            assert(lst == List(1, 3))
            val rs = comp.roadNetTables.roadNet.map(x => (x.start, x.end)).list
            assert(!rs.map(_._1).contains(2))
            assert(!rs.map(_._1).contains(4))
            assert(!rs.map(_._2).contains(2))
            assert(!rs.map(_._2).contains(4))
        }
        comp.database.withTransaction { implicit s: Session =>
            val lst = comp.roadNetTables.roadNet.list
            info(lst.toString)
            assert(lst.size == 1)
        }
    }
}