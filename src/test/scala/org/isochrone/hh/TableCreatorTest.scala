package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.scalatest.matchers.MustMatchers
import org.postgresql.util.PSQLException

class TableCreatorTest extends FunSuite with TestDatabase with MustMatchers {
    test("table creator creates tables") {
        val comp = new HHTableCreatorComponent with HHTableComponent with TestDatabaseComponent {
            val hhTables = new DefaultHHTablesWithPrefix("test_")
        }

        comp.HHTableCreator.createTables
        comp.database.withTransaction { implicit s: Session =>
            assert(Query(comp.hhTables.neighbourhoods).list.size === 0)
            assert(Query(comp.hhTables.shortcutEdges).list.size === 0)
        }
        comp.HHTableCreator.dropTables
        comp.database.withTransaction { implicit s: Session =>
            intercept[PSQLException] {
                Query(comp.hhTables.neighbourhoods).list
            }
        }
    }
}