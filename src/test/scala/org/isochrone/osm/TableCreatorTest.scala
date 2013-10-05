package org.isochrone.osm

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.db.DefaultVisualizationTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.VisualizationTableComponent
import org.postgresql.util.PSQLException

trait TestTableCreatorComponent
    extends TableCreatorComponent
    with TestDatabaseComponent
    with DefaultRoadNetTableComponent
    with DefaultVisualizationTableComponent

class TableCreatorTest extends FunSuite with TestDatabase {
    def queries(comp: RoadNetTableComponent with VisualizationTableComponent) = {
        import comp._
        Seq(Query(roadNetTables.roadNet),
            Query(roadNetTables.roadNodes),
            Query(visualizationTables.roadNetVisualization))
    }

    test("TableCreator creates tables") {
        val comp = new TestTableCreatorComponent {
            component =>
            tableCreator.create()

            database.withSession { implicit s: Session =>
                for (q <- queries(component)) {
                    assert(q.list.size == 0)
                }
            }
        }
    }

    test("TableCreator drops tables") {
        val comp = new TestTableCreatorComponent {
            component =>
            tableCreator.create()
            tableCreator.drop()

            database.withSession { implicit s: Session =>
                for (q <- queries(component)) {
                    intercept[PSQLException] {
                        q.list()
                    }
                }
            }
        }
    }
}