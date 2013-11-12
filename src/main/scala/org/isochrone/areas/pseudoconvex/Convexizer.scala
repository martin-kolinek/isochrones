package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase

trait ConvexizerComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    trait Convexizer {
        def convexize(ar: Area, diagonals: Traversable[EdgeWithCost])
    }
}