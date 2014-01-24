package org.isochrone.visualize

trait TranslatingProjectionComponent extends AzimuthalProjectionComponent {
    class TranslatingProjection(cx: Double, cy: Double) extends AzimuthalProjection {
        def project(lon: Double, lat: Double) = (lon - cx, lat - cy)
        def unproject(x: Double, y: Double) = (x + cx, y + cy)
    }

    def projectionForPoint(cx: Double, cy: Double) = new TranslatingProjection(cx, cy)
}