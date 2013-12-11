package org.isochrone.compute

import org.isochrone.graphlib.NodePositionComponent
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate

trait IsochroneOutputComponent {
    def isochroneGeometry: Traversable[Geometry]
}
