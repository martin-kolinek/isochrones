package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.areas.AreaReaderComponent

trait AreaNormalizerComponent extends AreaReaderComponent {
    self: GraphComponentBase =>
    trait AreaNormalizer extends AreaReader {
        abstract override def areas = super.areas.map(_.normalize)
    }
}