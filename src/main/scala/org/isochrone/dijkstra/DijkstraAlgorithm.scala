package org.isochrone.dijkstra

import org.isochrone.graphlib._
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.PriorityQueue
import scala.collection.mutable.TreeSet
import org.isochrone.util._
import org.isochrone.util.collection.mutable.IndexedPriorityQueue
import org.isochrone.compute.IsochroneComputerComponent
import org.isochrone.compute.SomeIsochroneComputerComponent

trait DijkstraAlgorithmComponent extends IsochroneComputerComponent with SomeIsochroneComputerComponent with DijkstraAlgorithmProviderComponent {
    self: GraphComponent =>

    val DijkstraAlgorithm = dijkstraForGraph(graph)

    val DijkstraHelpers = DijkstraAlgorithm.helper

    val isoComputer = new IsochroneComputer {
        def isochrone(start: Traversable[(NodeType, Double)], max: Double) = DijkstraAlgorithm.isochrone(start, max).map(IsochroneNode.tupled)
    }

}

