package org.isochrone.visualize

import org.scalatest.FunSuite
import java.awt.geom.Point2D

class VisualizationTest extends FunSuite {
    test("equidistant azimuthal projection works") {
        //info(VisualizationUtil.circle(17, 48, 0.1, 4).toString)
        val proj = new EquidistantAzimuthalProjection(48, 17)
        info(proj.unproject(0, 0).toString)
        assert(proj.unproject(0, 0) == (48, 17))
        val (x, y) = proj.unproject(1, 1)
        info((x, y).toString)
        val (shouldx, shouldy) = (48.000009404119396, 17.000008993203462)
        assert(math.abs(shouldx - x) < 0.000000001)
        assert(math.abs(shouldy - y) < 0.000000001)
    }
}