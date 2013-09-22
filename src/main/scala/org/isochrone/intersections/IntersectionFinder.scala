package org.isochrone.intersections

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.OsmTableComponent
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.db.DatabaseProvider

trait IntersectionFinderComponent {
    self: RoadNetTableComponent with OsmTableComponent with DatabaseProvider =>

    object IntersectionFinder {
        def intersectionsIn(top: Double, left: Double, bottom: Double, right: Double) = {
            for {
                e1 <- roadNetTables.roadNet
                e2 <- roadNetTables.roadNet
                n1s <- osmTables.nodes if n1s.id === e1.start
                n1e <- osmTables.nodes if n1e.id === e1.end
                n2s <- osmTables.nodes if n2s.id === e2.start
                n2e <- osmTables.nodes if n2e.id === e2.end
                if n1s.geom @&& makeBox(makePoint(left, top), makePoint(right, bottom))
                if n1e.geom @&& makeBox(makePoint(left, top), makePoint(right, bottom))
                if n2s.geom @&& makeBox(makePoint(left, top), makePoint(right, bottom))
                if n2e.geom @&& makeBox(makePoint(left, top), makePoint(right, bottom))
                if e1.start =!= e2.start && e1.end =!= e2.end
                if e1.start =!= e2.end && e1.end =!= e2.start
                geom1 = (n1s.geom shortestLine n1e.geom).asColumnOf[Geometry]
                geom2 = (n2s.geom shortestLine n2e.geom).asColumnOf[Geometry]
                if geom1 intersects geom2
            } yield (n1s.id, n1e.id, n2s.id, n2e.id)
        }

        def removeIntersections(top: Double, left: Double, bottom: Double, right: Double) = {
            database.withSession { implicit s: Session =>
                val intersecting = for ((s1, e1, s2, e2) <- intersectionsIn(top, left, bottom, right).list)
                    yield Set(Set(s1, e1), Set(s2, e2))
            }

        }
    }

}