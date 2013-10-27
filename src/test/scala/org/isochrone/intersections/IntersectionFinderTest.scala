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
import org.isochrone.osm.CostAssignerComponent
import com.vividsolutions.jts.geom.Geometry

class IntersectionFinderTest extends FunSuite with TestDatabase {
    trait TestFinder extends IntersectionFinderComponent with TestDatabaseComponent with RoadNetTableComponent with OsmTableComponent with CostAssignerComponent {
        def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = 3.0
        def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = 1.0
    }
    test("removing intersection works") {
        new TestFinder {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("test_")
            assert(IntersectionFinder.hasIntersections(2, -2, -2, 2))
            val totalcost = database.withSession { implicit s: Session =>
                Query(roadNetTables.roadNet).filter(x => x.start === 5000000000l && x.end === 5000000002l).map(_.cost).firstOption.get
            }
            IntersectionFinder.removeIntersections(2, -2, -2, 2)
            database.withTransaction { implicit s: Session =>
                val lst = Query(roadNetTables.roadNet).map(x => (x.start, x.end)).list
                info(lst.toString)
                assert(lst.size == lst.toSet.size)
            }
            assert(!IntersectionFinder.hasIntersections(2, -2, -2, 2))
            database.withSession { implicit s: Session =>
                val cst1 = Query(roadNetTables.roadNet).filter(x => x.start === 5000000000l && x.end === 5000000004l).map(_.cost).firstOption.get
                val cst2 = Query(roadNetTables.roadNet).filter(x => x.start === 5000000004l && x.end === 5000000002l).map(_.cost).firstOption.get
                val cst3 = Query(roadNetTables.roadNet).filter(x => x.start === 5000000001l && x.end === 5000000004l).map(_.cost).firstOption.get
                val cst4 = Query(roadNetTables.roadNet).filter(x => x.start === 5000000004l && x.end === 5000000003l).map(_.cost).firstOption.get
                assert(math.abs(cst1 - 3) < 0.0001)
                assert(math.abs(cst2 - 3) < 0.0001)
                assert(math.abs(cst3 - 1) < 0.0001)
                assert(math.abs(cst4 - 1) < 0.0001)
            }
        }
    }

    test("removing intersections does not create duplicate roads") {
        new TestFinder {
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("intersect_")
            assert(IntersectionFinder.hasIntersections(2, -2, -2, 2))
            IntersectionFinder.removeIntersections(2, -2, -2, 2)
            database.withTransaction { implicit s: Session =>
                val lst = Query(roadNetTables.roadNet).map(x => (x.start, x.end)).list
                info(lst.toString)
                assert(lst.size == lst.toSet.size)
            }
            assert(!IntersectionFinder.hasIntersections(2, -2, -2, 2))
        }
    }
}