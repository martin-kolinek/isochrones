package org.isochrone.compute

import org.isochrone.graphlib.NodePositionComponent
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate

trait IsochroneOutputComponent {
    def isochroneGeometry: Traversable[Geometry]
}

trait PointIsochroneOutput extends IsochroneOutputComponent {
    self: NodePositionComponent with IsochronesComputationComponent =>
    private val geomFact = new GeometryFactory(new PrecisionModel, 4326)
    def isochroneGeometry = isochrone.map { n =>
        val (x, y) = nodePos.nodePosition(n.nd)
        geomFact.createPoint(new Coordinate(x, y))
    }
}