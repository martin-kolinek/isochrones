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

    def project(lond: Double, latd: Double): (Double, Double) = {
        import math._
        val lat = toRadians(latd)
        val lon = toRadians(lond)
        val c = acos(sin(cyrad) * sin(lat) + cos(cyrad) * cos(lat) * cos(lon - cxrad))
        val k = c / sin(c)
        val x = k * cos(lat) * sin(lon - cxrad)
        val y = k * (cos(cyrad) * sin(lat) - sin(cyrad) * cos(lat) * cos(lon - cxrad))
        (x * 6371008.77141506, y * 6371008.77141506)
    }
}