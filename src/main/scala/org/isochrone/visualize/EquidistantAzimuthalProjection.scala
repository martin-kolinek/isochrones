package org.isochrone.visualize

class EquidistantAzimuthalProjection(cx: Double, cy: Double) {
    val cxrad = math.toRadians(cx)
    val cyrad = math.toRadians(cy)
    def unproject(xx: Double, yy: Double): (Double, Double) = {
        val x = xx / 6371008.77141506
        val y = yy / 6371008.77141506
        import math._
        val c = sqrt(x * x + y * y)
        if (c == 0)
            (cx, cy)
        else {
            val phi = asin(cos(c) * math.sin(cyrad) + (y * sin(c) * cos(cyrad)) / c)
            val lambda = cxrad + atan((x * sin(c)) / (c * cos(cyrad) * cos(c) - y * sin(cyrad) * sin(c)))
            (toDegrees(lambda), toDegrees(phi))
        }
    }
}