package org.isochrone.dbgraph

import org.scalatest.FunSuite
import resource._
import scala.slick.driver.SQLiteDriver.simple._
import org.isochrone.db.TestDatabase
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.osm.TableCreatorComponent
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.VisualizationTableComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.DefaultVisualizationTableComponent
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate

trait RoadNetTableCreation extends BeforeAndAfterEach {
    self: Suite =>
    abstract override def beforeEach() {
        super.beforeEach()
        new TableCreatorComponent with TestDatabaseComponent with DefaultRoadNetTableComponent with OsmTableComponent with DefaultVisualizationTableComponent {
            tableCreator.create()
        }
    }
}

class DbGraphTest extends FunSuite with TestDatabase with RoadNetTableCreation {
    val geomfact = new GeometryFactory(new PrecisionModel, 4326)
    test("DatabaseGraph retrieves neighbours") {
        new DefaultRoadNetTableComponent with TestDatabaseComponent {
            database.withSession { implicit s: Session =>
                (1l to 5l).map((_, 1, geomfact.createPoint(new Coordinate(0, 0)))).foreach(roadNetTables.roadNodes.insert(_))
                roadNetTables.roadNet.insertAll(
                    (1, 2, 0.1, false),
                    (1, 3, 0.2, false),
                    (2, 4, 0.3, false),
                    (3, 2, 0.4, false),
                    (2, 1, 0.5, false),
                    (4, 5, 0.6, false))
            }
        }

        new DefaultRoadNetTableComponent with DefaultDatabaseGraphComponent with SingleSessionProvider with TestDatabaseComponent {
            val neigh = graph.neighbours(2)
            assert(neigh.size == 2)
            assert(neigh.toSet == Set((1, 0.5), (4, 0.3)))
        }
    }

    test("DatabaseGraph return empty list for nonexistent nodes)") {
        new DefaultRoadNetTableComponent with TestDatabaseComponent {
            database.withSession { implicit session: Session =>
                (1l to 5l).map((_, 1, geomfact.createPoint(new Coordinate(0, 0)))).foreach(roadNetTables.roadNodes.insert(_))
                roadNetTables.roadNet.insertAll(
                    (1, 2, 0.1, false),
                    (1, 3, 0.2, false),
                    (2, 4, 0.3, false),
                    (3, 2, 0.4, false),
                    (2, 1, 0.5, false),
                    (4, 5, 0.6, false))
            }
        }

        new DefaultRoadNetTableComponent with DefaultDatabaseGraphComponent with SingleSessionProvider with TestDatabaseComponent {
            assert(graph.neighbours(5).size == 0)
            val neigh = graph.neighbours(10)
            assert(neigh.size == 0)
        }
    }

    test("DatabaseGraph does not retrieve region multiple times") {
        new DefaultRoadNetTableComponent with TestDatabaseComponent {
            database.withSession { implicit session: Session =>
                (1l to 5l).map((_, 1, geomfact.createPoint(new Coordinate(0, 0)))).foreach(roadNetTables.roadNodes.insert(_))
                roadNetTables.roadNet.insertAll(
                    (1, 2, 0.1, false),
                    (1, 3, 0.2, false),
                    (2, 4, 0.3, false),
                    (3, 2, 0.4, false),
                    (2, 1, 0.5, false),
                    (4, 5, 0.6, false))
            }
        }
        new DefaultRoadNetTableComponent with DefaultDatabaseGraphComponent with SingleSessionProvider with TestDatabaseComponent {
            assert(graph.neighbours(5).size == 0)
            assert(graph.neighbours(5).size == 0)
            assert(graph.neighbours(5).size == 0)
            val neigh = graph.neighbours(10)
            assert(neigh.size == 0)
            assert(graph.retrievals > 0)
            assert(graph.retrievals <= 2)
        }
    }

    test("DatabaseGraph keeps right amount of regions") {
        new DefaultRoadNetTableComponent with TestDatabaseComponent {
            database.withSession { implicit session: Session =>
                roadNetTables.roadNodes.insertAll(
                    (1, 1, geomfact.createPoint(new Coordinate(0, 0))),
                    (2, 1, geomfact.createPoint(new Coordinate(0, 0))),
                    (3, 2, geomfact.createPoint(new Coordinate(0, 0))),
                    (4, 2, geomfact.createPoint(new Coordinate(0, 0))),
                    (5, 2, geomfact.createPoint(new Coordinate(0, 0))))
                roadNetTables.roadNet.insertAll(
                    (1, 2, 0.1, false),
                    (2, 3, 0.2, false),
                    (2, 4, 0.3, false),
                    (5, 2, 0.4, false),
                    (5, 3, 0.5, false),
                    (4, 5, 0.6, false),
                    (3, 5, 0.7, false))
            }
        }
        new DatabaseGraphComponent with SingleSessionProvider with TestDatabaseComponent with DefaultRoadNetTableComponent {
            val graph = new DatabaseGraph(roadNetTables, 1)
            val neigh = graph.neighbours(2)
            assert(neigh.size == 2)
            assert(neigh.toSet == Set((3, 0.2), (4, 0.3)))
            assert(graph.nodesInMemory == 2)
            val neigh2 = graph.neighbours(5)
            assert(neigh2.size == 2)
            assert(neigh2.toSet == Set((2, 0.4), (3, 0.5)))
            info(graph.nodesInMemory.toString)
            assert(graph.nodesInMemory == 3)
        }
    }
}
