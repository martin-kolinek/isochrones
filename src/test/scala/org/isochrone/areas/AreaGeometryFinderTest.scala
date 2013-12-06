package org.isochrone.areas

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponentBase

class AreaGeometryFinderTest extends FunSuite {
    test("AreaGeometryFinder works on simple area") {
        new AreaGeometryFinderComponent with PtsComponent {

            val ar = PosArea(0, List(0, 1, 2, 3).map(pts), Map())
            val filtered = AreaGeometryFinder.filterInnerNodes(ar)
            assert(filtered.area == Area(List(0, 1, 2, 3)))
            assert(AreaGeometryFinder.areaGeometry(ar).isValid)
        }
    }

    test("AreaGeometryFinder works on more difficult area") {
        new AreaGeometryFinderComponent with PtsComponent {
            val ar = PosArea(0, List(1, 4, 5, 4, 6, 4, 1, 2, 3, 0).map(pts), Map())
            val filtered = AreaGeometryFinder.filterInnerNodes(ar)
            assert(filtered.area == Area(List(1, 2, 3, 0)))
            val geom = AreaGeometryFinder.areaGeometry(ar)
            info(geom.toString)
            assert(geom.isValid)
        }
    }

    test("AreaGeometryFinder works on area with two inner nodes") {
        new AreaGeometryFinderComponent with PtsComponent {
            val ar = PosArea(0, List(0, 1, 5, 1, 2, 6, 2, 3).map(pts), Map())
            val filtered = AreaGeometryFinder.filterInnerNodes(ar)
            assert(filtered.area == Area(List(0, 1, 2, 3)))
            assert(AreaGeometryFinder.areaGeometry(ar).isValid)
        }
    }
}