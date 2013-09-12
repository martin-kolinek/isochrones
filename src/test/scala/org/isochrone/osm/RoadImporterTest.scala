package org.isochrone.osm

import org.scalatest.FunSuite
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.TestDatabase
import org.isochrone.util.db.MyPostgresDriver.simple._

trait TestRoadImporterComponent extends RoadImporterComponent with TestTableCreatorComponent with OsmTableComponent

class RoadImporterTest extends FunSuite with TestDatabase {
    test("RoadImporter imports roads") {
        val comp = new TestRoadImporterComponent {
            tableCreator.execute()
            roadImporter.execute()
            database.withSession { implicit s: Session =>
                assert(Query(roadNetTables.roadNodes).filter(_.id === 262930213l).list.size == 1)
                //some nodes from way 24282589
                assert(Query(roadNetTables.roadNet).filter(x => x.start === 262930213l && x.end === 262930214l).list.size == 1)
                assert(Query(roadNetTables.roadNet).filter(x => x.end === 262930213l && x.start === 262930214l).list.size == 1)
                assert(Query(roadNetTables.roadNetUndir).filter(x => x.start === 262930213l && x.end === 262930214l).list.size == 1)
                assert(Query(roadNetTables.roadNetUndir).filter(x => x.end === 262930213l && x.start === 262930214l).list.size == 1)
                //some nodes from way 194749394
                assert(Query(roadNetTables.roadNet).filter(x => x.start === 249661252l && x.end === 55466850l).list.size == 1)
                assert(Query(roadNetTables.roadNet).filter(x => x.end === 249661252l && x.start === 55466850l).list.size == 0)
                assert(Query(roadNetTables.roadNetUndir).filter(x => x.start === 249661252l && x.end === 55466850l).list.size == 1)
                assert(Query(roadNetTables.roadNetUndir).filter(x => x.end === 249661252l && x.start === 55466850l).list.size == 1)
            }
        }
    }
}