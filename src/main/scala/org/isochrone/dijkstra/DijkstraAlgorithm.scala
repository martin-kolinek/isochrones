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

trait DijkstraAlgorithmComponent extends IsochroneComputerComponent with SomeIsochroneComputerComponent {
    self: GraphComponent =>

    object DijkstraAlgorithm extends IsochroneComputer {

        def alg(start: Traversable[(NodeType, Double)], closedFunc: (NodeType, Double, Option[(NodeType, Double)]) => Unit, opened: (NodeType, NodeType) => Unit, cancel: () => Boolean) {
            val closed = new HashSet[NodeType]
            val costMap = new HashMap[NodeType, Double]
            val previous = new HashMap[NodeType, (NodeType, Double)]
            val open = IndexedPriorityQueue(start.toSeq: _*)
            costMap ++= start
            while (!open.empty && !cancel()) {
                val (current, curCost) = open.minimum
                open -= current
                closed += current
                closedFunc(current, curCost, previous.get(current))
                for ((neighbour, cost) <- graph.neighbours(current) if !closed.contains(neighbour)) {
                    val newCost = curCost + cost
                    val better = costMap.get(neighbour).map(newCost < _)
                    if (better.getOrElse(false)) {
                        open -= neighbour
                    }
                    if (better.getOrElse(true)) {
                        opened(neighbour, current)
                        open += neighbour -> newCost
                        previous(neighbour) = current -> cost
                    }
                }
            }
        }

        def compute(start: Traversable[(NodeType, Double)]) = new Traversable[(NodeType, Double)] {
            self =>
            def foreach[U](func: ((NodeType, Double)) => U) {
                alg(start, (node, cost, prev) => func(node -> cost), (x, y) => {}, () => false)
            }
        }

        def nodesWithin(start: Traversable[(NodeType, Double)], max: Double) = compute(start).takeWhile(_._2 <= max)

        def isochrone(start: Traversable[(NodeType, Double)], max: Double) = {
            nodesWithin(start, max).map {
                case (id, fromStart) => IsochroneNode(id, max - fromStart)
            }
        }
    }

    object DijkstraHelpers {
        def isochrone(start: NodeType, max: Double) =
            DijkstraAlgorithm.isochrone(Traversable(start -> 0.0), max)
        def nodesWithin(start: NodeType, max: Double) =
            DijkstraAlgorithm.nodesWithin(Traversable(start -> 0.0), max)
        def distance(start: NodeType, end: NodeType) =
            DijkstraAlgorithm.compute(Traversable(start -> 0.0)).
                lazyFilter(_._1 == end).
                headOption.map(_._2).
                getOrElse(Double.MaxValue)
        def compute(start: NodeType) = DijkstraAlgorithm.compute(Traversable(start -> 0.0))
    }

    val isoComputer = DijkstraAlgorithm

}

