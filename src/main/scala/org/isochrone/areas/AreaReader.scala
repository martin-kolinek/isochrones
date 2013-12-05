package org.isochrone.areas

import org.isochrone.graphlib.GraphComponentBase

trait AreaReaderComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    trait AreaReader {
        def areas: TraversableOnce[Area]
    }

    val reader: AreaReader
}