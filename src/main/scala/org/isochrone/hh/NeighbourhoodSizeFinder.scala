package org.isochrone.hh

import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.graphlib.GraphComponentBase

trait NeighbourhoodSizeFinderComponent {
    self: DijkstraAlgorithmComponent with GraphComponentBase =>

    object NeighbourhoodSizeFinder {
        def findNeighbourhoodSize(nd: NodeType, count: Int) = {
            DijkstraHelpers.compute(nd).view.drop(count - 1).head._2
        }
    }
}