package org.isochrone.visualize

trait AzimuthalProjectionComponent {
    trait AzimuthalProjection {
        def unproject(x: Double, y: Double): (Double, Double)
        def project(lon: Double, lat: Double): (Double, Double)
    }

    def projectionForPoint(cx: Double, cy: Double): AzimuthalProjection
}