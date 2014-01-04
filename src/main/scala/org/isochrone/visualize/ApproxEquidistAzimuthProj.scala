package org.isochrone.visualize

class ApproxEquidistAzimuthProj(cx: Double, cy: Double) {
    val cxrad = math.toRadians(cx)
    val cyrad = math.toRadians(cy)

    val mericirc = 40007860.0 //m
    val eqcirc = 40075017.0 //m
    val xscale: Double = {
        val circ = math.cos(cyrad) * eqcirc
        (1 / circ * 360)
    }
    val yscale: Double = (1 / mericirc) * 360

    def unproject(x: Double, y: Double) = {
        val lon = x * xscale + cx
        val lat = y * yscale + cy
        (lon, lat)
    }

    def project(lon: Double, lat: Double) = {
        val x = (lon - cx) / xscale
        val y = (lat - cy) / yscale
        (x, y)
    }

}