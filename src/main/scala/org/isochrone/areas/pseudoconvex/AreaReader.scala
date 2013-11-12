package org.isochrone.areas.pseudoconvex

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.graphlib.GraphComponentBase

trait AreaReaderComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    trait AreaReader {
        def areas: TraversableOnce[Area]
    }

    val reader: AreaReader
}