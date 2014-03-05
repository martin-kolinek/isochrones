package org.isochrone.osm

import org.scalatest.FunSuite
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.TestDatabase
import org.isochrone.connect.GraphConnectorComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.HigherLevelRoadNetTableComponent
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.VisualizationTableComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
trait TestRoadNetVisualizerComponent extends RoadNetVisualizerComponent with TestRoadImporterComponent with DefaultCostAssignerComponent

class RoadNetVisualizerTest extends FunSuite with TestDatabase {
    test("RoadNetVisualizer works") {
        val comp = new TestRoadNetVisualizerComponent {
            tableCreator.create()
            roadImporter.execute()
            visualizer.execute()
            database.withSession { implicit s: Session =>
                val lst = visualizationTables.roadNetVisualization.filter(x => x.start === 262930213l && x.end === 262930214l || x.end === 262930213l && x.start === 262930214l).map(_.direction).list()
                assert(lst.size == 1)
                assert(lst.head == 0)
                val lst2 = visualizationTables.roadNetVisualization.filter(x => x.start === 249661252l && x.end === 55466850l).map(_.direction).list()
                assert(lst2.size == 1)
                assert(lst2.head == 1)
                val lst3 = visualizationTables.roadNetVisualization.filter(x => x.start === 55466850l && x.end === 249661252l).map(_.direction).list()
                assert(lst3.size == 0)
            }
        }
    }

    test("RoadNetVisualizaer visualizes virtual roads") {
        trait TestVisComp extends VisualizationTableComponent {
            val visualizationTables = new VisualizationTables {
                val roadNetVisualization = TableQuery(t => new RoadNetVisualization(t, "con_test_vis"))
            }
        }

        val creat = new TableCreatorComponent with RoadNetTableComponent with TestVisComp with TestDatabaseComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("vadfgasd")
        }
        creat.tableCreator.create()

        val comp = new RoadNetVisualizerComponent with GraphConnectorComponent with RoadNetTableComponent with HigherLevelRoadNetTableComponent with TestDatabaseComponent with TestVisComp with DefaultCostAssignerComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("con_")
            val higherRoadNetTables = new DefaultRoadNetTablesWithPrefix("con_higher_")

        }

        comp.GraphConnector.connectGraph()
        comp.visualizer.execute()

        comp.database.withSession { implicit s: Session =>
            val lst = comp.visualizationTables.roadNetVisualization.filter(x => x.start === 3l && x.end === 1l || x.start === 1l && x.end === 3l).map(_.direction).list
            info(lst.toString)
            assert(lst == List(2))
        }
    }
}