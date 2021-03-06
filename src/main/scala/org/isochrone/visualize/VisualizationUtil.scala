package org.isochrone.visualize

import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate
import org.isochrone.util._
import spire.syntax.normedVectorSpace._
import spire.std.seq._
import spire.std.double._
import com.vividsolutions.jts.geom.LineString

object VisualizationUtil {
    //r1 - radius of left circle
    //r2 - radius of right circle
    //d - distance between circles
    //taken from internet
    private def circleIntersections(r1: Double, r2: Double, d: Double) = {
        val x = (d * d - r2 * r2 + r1 * r1) / (2 * d)
        val y = {
            val first = -d + r2 - r1
            val second = -d - r2 + r1
            val third = -d + r2 + r1
            val fourth = d + r2 + r1
            val a = (1 / d) * math.sqrt(first * second * third * fourth)
            a / 2
        }
        Seq(vector(x, -y), vector(x, y))
    }

    def circleIntersection(c1: List[Double], c2: List[Double], r1: Double, r2: Double): Seq[List[Double]] = {
        val direction = (c2 - c1).normalize
        val normal = vector(direction.y, -direction.x).normalize
        val d = (c2 - c1).norm
        for (List(x, y) <- circleIntersections(r1, r2, d))
            yield c1 + (direction :* x) + (normal :* y)
    }
}