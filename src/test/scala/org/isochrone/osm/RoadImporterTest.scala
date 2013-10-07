package org.isochrone.osm

import org.scalatest.FunSuite
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.TestDatabase
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry

trait TestRoadImporterComponent extends RoadImporterComponent with TestTableCreatorComponent with OsmTableComponent {
    self: CostAssignerComponent =>
}

class RoadImporterTest extends FunSuite with TestDatabase {
    test("RoadImporter imports roads") {
        val comp = new TestRoadImporterComponent with CostAssignerComponent {
            def getRoadCost(c1:Column[Geometry], c2:Column[Geometry]) = 3.0
            def getNoRoadCost(c1:Column[Geometry], c2:Column[Geometry]) = 6.0
            tableCreator.create()
            roadImporter.execute()
            database.withSession { implicit s: Session =>
                assert(Query(roadNetTables.roadNodes).filter(_.id === 262930213l).list.size == 1)
                //some nodes from way 24282589
                val l3 = Query(roadNetTables.roadNet).filter(x => x.start === 262930213l && x.end === 262930214l).list
                assert(l3.size == 1)
                assert(math.abs(l3.head._3 - 3.0) < 0.0001)
                assert(!l3.head._4)
                val l4 = Query(roadNetTables.roadNet).filter(x => x.end === 262930213l && x.start === 262930214l).list
                assert(l4.size == 1)
                assert(math.abs(l4.head._3 - 3.0) < 0.0001)
                assert(!l4.head._4)
                //some nodes from way 194749394
                val l1 = Query(roadNetTables.roadNet).filter(x => x.start === 249661252l && x.end === 55466850l).list
                assert(l1.size == 1)
                assert(math.abs(l1.head._3 - 3.0) < 0.0001)
                assert(!l1.head._4)
                val l2 = Query(roadNetTables.roadNet).filter(x => x.end === 249661252l && x.start === 55466850l).list
                assert(l2.size == 1)
                assert(math.abs(l2.head._3 - 6.0) < 0.0001)
                assert(l2.head._4)
            }
        }
    }
}