package org.isochrone.dijkstra

import org.isochrone.graphlib.GraphComponent
import org.isochrone.util._
import org.isochrone.graphlib.GraphType
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.compute.IsochroneComputerComponent
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import org.isochrone.util.collection.mutable.IndexedPriorityQueue
import com.typesafe.scalalogging.slf4j.Logging

class DijkstraHelperClass[NodeType](dijk: DijkstraAlgorithmClass[NodeType]) {
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

class DijkstraAlgorithmClass[NodeType](graph: GraphType[NodeType])(implicit ndOrd: Ordering[NodeType]) extends Logging {
    import Ordering.Tuple2
    def alg(start: Traversable[(NodeType, Double)], closedFunc: (NodeType, Double, Option[(NodeType, Double)]) => Unit, opened: (NodeType, NodeType, Double) => Unit, cancel: () => Boolean) {
        val closed = new HashSet[NodeType]
        val costMap = new HashMap[NodeType, Double]
        val previous = new HashMap[NodeType, (NodeType, Double)]
        val open = IndexedPriorityQueue(start.map(x => (x._1, (x._2, x._1))).toSeq: _*)
        costMap ++= start
        while (!open.empty && !cancel()) {
            val (current, (curCost, _)) = open.minimum
            logger.debug(s"Closing $current with $curCost from ${previous.get(current)}")
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
                    opened(neighbour, current, newCost)
                    logger.debug(s"Opening $neighbour with $newCost")
                    costMap(neighbour) = newCost
                    open += neighbour -> (newCost -> neighbour)
                    previous(neighbour) = current -> cost
                }
            }
        }
    }

    def compute(start: Traversable[(NodeType, Double)]) = new Traversable[(NodeType, Double)] {
        self =>
        def foreach[U](func: ((NodeType, Double)) => U) {
            alg(start, (node, cost, prev) => func(node -> cost), (x, y, z) => {}, () => false)
        }
    }

    def nodesWithin(start: Traversable[(NodeType, Double)], max: Double) = compute(start).takeWhile(_._2 <= max)

    def isochrone(start: Traversable[(NodeType, Double)], max: Double) = {
        nodesWithin(start, max).map {
            case (id, fromStart) => (id, max - fromStart)
        }
    }

    val helper = new DijkstraHelperClass(this)
}

trait GenericDijkstraAlgorithmProvider {
    def dijkstraForGraph[NodeType: Ordering](g: GraphType[NodeType]) = new DijkstraAlgorithmClass[NodeType](g)
}

trait DijkstraAlgorithmProviderComponent {
    self: GraphComponentBase =>

    def dijkstraForGraph(g: GraphType[NodeType]): DijkstraAlgorithmClass[NodeType] = new DijkstraAlgorithmClass[NodeType](g)(Ordering.by(_.hashCode))
}