package org.isochrone.hh

import org.isochrone.graphlib.GraphComponentBase

trait NeighbourhoodSizes[NodeType] {
    def neighbourhoodSize(nd: NodeType): Double
}

trait NeighbourhoodSizeComponent {
    self: GraphComponentBase =>
    val neighbourhoods: NeighbourhoodSizes[NodeType]
}