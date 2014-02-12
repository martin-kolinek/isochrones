package org.isochrone.dijkstra

import org.isochrone.graphlib.GraphComponent
import org.isochrone.util._
import org.isochrone.graphlib.GraphType
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.compute.IsochroneComputerComponent
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import org.isochrone.util.collection.mutable.IndexedPriorityQueue

trait DijkstraAlgorithmProviderComponent extends IsochroneComputerComponent {
    self: GraphComponentBase =>

    def dijkstraForGraph(g: GraphType[NodeType]): DijkstraAlgorithmClass = new DijkstraAlgorithmClass(g)

    class DijkstraHelperClass(dijk: DijkstraAlgorithmClass) {
        def isochrone(start: NodeType, max: Double) =
            dijk.isochrone(Traversable(start -> 0.0), max)
        def nodesWithin(start: NodeType, max: Double) =
            dijk.nodesWithin(Traversable(start -> 0.0), max)
        def distance(start: NodeType, end: NodeType) =
            dijk.compute(Traversable(start -> 0.0)).
                lazyFilter(_._1 == end).
                headOption.map(_._2).
                getOrElse(Double.MaxValue)
        def compute(start: NodeType) = dijk.compute(Traversable(start -> 0.0))
    }

    class DijkstraAlgorithmClass(graph: GraphType[NodeType]) extends IsochroneComputer {
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

        val helper = new DijkstraHelperClass(this)
    }
}