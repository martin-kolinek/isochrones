package org.isochrone.intersections

import org.scalatest.FunSuite
import scala.slick.jdbc.StaticQuery.interpolation
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.RoadNetTables
import org.isochrone.db.EdgeTable
import org.isochrone.db.NodeTable
import org.isochrone.db.RegionTable
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.DefaultRoadNetTablesWithPrefix

class IntersectionFinderTest extends FunSuite with TestDatabase {
    test("removing intersection works") {
        new IntersectionFinderComponent with TestDatabaseComponent with RoadNetTableComponent with OsmTableComponent {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("test_")

            assert(IntersectionFinder.hasIntersections(2, -2, -2, 2))
            val totalcost = database.withSession { implicit s: Session =>
                Query(roadNetTables.roadNet).filter(x => x.start === 5000000000l && x.end === 5000000002l).map(_.cost).firstOption.get
            }
            IntersectionFinder.removeIntersections(2, -2, -2, 2)
            assert(!IntersectionFinder.hasIntersections(2, -2, -2, 2))
            database.withSession { implicit s: Session =>
                val cst1 = Query(roadNetTables.roadNet).filter(x => x.start === 5000000000l && x.end === 5000000004l).map(_.cost).firstOption.get
                val cst2 = Query(roadNetTables.roadNet).filter(x => x.start === 5000000004l && x.end === 5000000002l).map(_.cost).firstOption.get
                assert(math.abs(cst1 + cst2 - totalcost) < 0.001)
            }
        }
    }
}