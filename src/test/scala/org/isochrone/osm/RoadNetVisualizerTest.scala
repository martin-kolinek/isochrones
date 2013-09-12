package org.isochrone.osm

import org.scalatest.FunSuite
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.TestDatabase
trait TestRoadNetVisualizerComponent extends RoadNetVisualizerComponent with TestRoadImporterComponent

class RoadNetVisualizerTest extends FunSuite with TestDatabase {
    test("RoadNetVisualizer works") {
        val comp = new TestRoadNetVisualizerComponent {
            tableCreator.execute()
            roadImporter.execute()
            visualizer.execute()
            database.withSession { implicit s: Session =>
                val lst = Query(visualizationTables.roadNetVisualization).filter(x => x.start === 262930213l && x.end === 262930214l || x.end === 262930213l && x.start === 262930214l).map(_.direction).list()
                assert(lst.size == 1)
                assert(lst.head == 0)
                val lst2 = Query(visualizationTables.roadNetVisualization).filter(x => x.start === 249661252l && x.end === 55466850l).map(_.direction).list()
                assert(lst2.size == 1)
                assert(lst2.head == 1)

                val lst3 = Query(visualizationTables.roadNetUndirVisualization).filter(x => x.start === 262930213l && x.end === 262930214l || x.end === 262930213l && x.start === 262930214l).map(_.direction).list()
                assert(lst3.size == 1)
                assert(lst3.head == 0)
                val lst4 = Query(visualizationTables.roadNetUndirVisualization).filter(x => x.start === 249661252l && x.end === 55466850l || x.end === 249661252l && x.start === 55466850l ).map(_.direction).list()
                assert(lst4.size == 1)
                assert(lst4.head == 0)
            }
        }
    }
}