package org.isochrone.areas

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponentBase
import com.vividsolutions.jts.geom.Polygon

class AreaGeometryFinderTest extends FunSuite {
    test("AreaGeometryFinder works on simple area") {
        new AreaGeometryFinderComponent with PtsComponent {

            val ar = PosArea(0, List(0, 1, 2, 3).map(pts), Map())
            val filtered = AreaGeometryFinder.extractAreas(ar)
            assert(filtered.size == 1)
            val area = filtered.head
            assert(area.area == Area(List(0, 1, 2, 3)))
            assert(AreaGeometryFinder.areaGeometry(ar).isValid)
        }
    }

    test("AreaGeometryFinder works on more difficult area") {
        new AreaGeometryFinderComponent with PtsComponent {
            val ar = PosArea(0, List(1, 4, 5, 4, 6, 4, 1, 2, 3, 0).map(pts), Map())
            val filtered = AreaGeometryFinder.extractAreas(ar)
            assert(filtered.size == 1)
            assert(filtered.head.area == Area(List(1, 2, 3, 0)))
            val geom = AreaGeometryFinder.areaGeometry(ar)
            info(geom.toString)
            assert(geom.isValid)
        }
    }

    test("AreaGeometryFinder works on area with two inner nodes") {
        new AreaGeometryFinderComponent with PtsComponent {
            val ar = PosArea(0, List(0, 1, 5, 1, 2, 6, 2, 3).map(pts), Map())
            val filtered = AreaGeometryFinder.extractAreas(ar)
            assert(filtered.size == 1)
            assert(filtered.head.area == Area(List(0, 1, 2, 3)))
            assert(AreaGeometryFinder.areaGeometry(ar).isValid)
        }
    }

    test("AreaGeometryFinder works on areas with holes") {
        new AreaGeometryFinderComponent with PtsComponent {
            val ar = PosArea(0, List(1, 2, 3, 4, 5, 7, 4, 3, 0).map(pts), Map())
            val filtered = AreaGeometryFinder.extractAreas(ar)
            assert(filtered.size == 2)
            assert(filtered.map(_.area).toSet == Set(Area(List(0, 1, 2, 3)), Area(List(4, 5, 7))))
            val geom = AreaGeometryFinder.areaGeometry(ar)
            assert(geom.isValid)
            assert(geom.isInstanceOf[Polygon])
        }
    }

    test("AreaGeometryFinder works on rotated areas with holes") {
        new AreaGeometryFinderComponent with PtsComponent {
            val ar = PosArea(0, List(7, 4, 3, 0, 1, 2, 3, 4, 5).map(pts), Map())
            val filtered = AreaGeometryFinder.extractAreas(ar)
            assert(filtered.size == 2)
            assert(filtered.map(_.area).toSet == Set(Area(List(0, 1, 2, 3)), Area(List(4, 5, 7))))
            val geom = AreaGeometryFinder.areaGeometry(ar)
            assert(geom.isValid)
            assert(geom.isInstanceOf[Polygon])
        }
    }

    test("AreaGeometryFinder works on area with hole touching the shell") {
        new AreaGeometryFinderComponent with PtsComponent {
            val ar = PosArea(0, List(1, 2, 3, 6, 8, 9, 6, 4, 3, 7, 5, 3, 0).map(pts), Map())
            val filtered = AreaGeometryFinder.extractAreas(ar)
            assert(filtered.size == 4)
            assert(filtered.map(_.area).toSet == Set(Area(List(0, 1, 2, 3)),
                Area(List(7, 5, 3)),
                Area(List(6, 4, 3)),
                Area(List(6, 8, 9))))
            val geom = AreaGeometryFinder.areaGeometry(ar)
            assert(geom.isValid)
            assert(geom.isInstanceOf[Polygon])
        }
    }
}