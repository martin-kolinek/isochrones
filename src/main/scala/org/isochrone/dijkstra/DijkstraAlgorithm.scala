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

        private def alg(start: Traversable[(NodeType, Double)], res: (NodeType, Double) => Unit) {
            val closed = new HashSet[NodeType]
            val costMap = new HashMap[NodeType, Double]
            val open = IndexedPriorityQueue(start.toSeq: _*)
            costMap ++= start

            while (!open.empty) {
                val (current, curCost) = open.minimum
                open -= current
                closed += current
                res(current, curCost)
                for ((neighbour, cost) <- graph.neighbours(current) if !closed.contains(neighbour)) {
                    val newCost = curCost + cost
                    val better = costMap.get(neighbour).map(newCost < _)
                    if (better.getOrElse(false)) {
                        open -= neighbour
                    }
                    if (better.getOrElse(true)) {
                        open += neighbour -> newCost
                    }
                }
            }
        }

        def compute(start: Traversable[(NodeType, Double)]) = new Traversable[(NodeType, Double)] {
            def foreach[U](func: ((NodeType, Double)) => U) {
                alg(start, (x: NodeType, y: Double) => func(x -> y))
            }
        }

        def nodesWithin(start: Traversable[(NodeType, Double)], max: Double) = compute(start).takeWhile(_._2 <= max)

        def isochrone(start: Traversable[(NodeType, Double)], max: Double) = {
            val nodes = nodesWithin(start, max).toSeq
            val inIsoSet = nodes.map(_._1).toSet
            nodes.flatMap {
                case (nd, cost) => {
                    val remaining = max - cost
                    for {
                        (neigh, ncost) <- graph.neighbours(nd)
                        if !inIsoSet.contains(neigh)
                        quotient = remaining / ncost
                        if quotient <= 1
                    } yield IsochroneEdge(nd, neigh, quotient)
                }
            }
        }
    }

    object DijkstraHelpers {
        def isochrone(start: NodeType, max: Double) =
            DijkstraAlgorithm.isochrone(Traversable(start -> 0.0), max)
        def nodesWithin(start: NodeType, max: Double) =
            DijkstraAlgorithm.nodesWithin(Traversable(start -> 0.0), max)
    }

    val isoComputer = DijkstraAlgorithm

}

