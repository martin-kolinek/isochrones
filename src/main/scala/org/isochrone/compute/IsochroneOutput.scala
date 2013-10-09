package org.isochrone.compute

import org.isochrone.graphlib.NodePositionComponent
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate

trait IsochroneOutputComponent {
    def isochroneGeometry: Traversable[Geometry]
}

trait PointIsochroneOutputComponent extends IsochroneOutputComponent {
    self: NodePositionComponent with IsochronesComputationComponent =>

    val geomFact = new GeometryFactory(new PrecisionModel(), 4326)

    lazy val isochroneGeometry = for {
        IsochroneEdge(a, b, part) <- isochrone
        (x1, y1) <- graph.nodePosition(a)
        (x2, y2) <- graph.nodePosition(b)
        x = x1 + (x2 - x1) * part
        y = y1 + (y2 - y1) * part
    } yield geomFact.createPoint(new Coordinate(x, y))

}