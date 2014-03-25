package org.isochrone.hh

import scala.collection.immutable.Queue
import scala.annotation.tailrec
import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.GraphType
import com.typesafe.scalalogging.slf4j.Logging

trait SecondPhaseComponent {
    self: FirstPhaseComponent with GraphComponentBase =>

    def secondPhase(graph: GraphType[NodeType], neighSizes: NeighbourhoodSizes[NodeType]) = new SecondPhase(graph, neighSizes)

    class SecondPhase(graph: GraphType[NodeType], neighSizes: NeighbourhoodSizes[NodeType]) extends Logging {
        def extractHighwayEdges(tree: NodeTree) = {
            @tailrec
            def processQueue(q: Queue[NodeType], edges: List[(NodeType, NodeType)], slacks: Map[NodeType, Double]): List[(NodeType, NodeType)] = {
                if (q.isEmpty)
                    edges
                else {
                    val (nd, dequeued) = q.dequeue
                    val parent = tree.parentMap(nd)
                    val parentSlack = slacks(nd) - graph.edgeCost(nd, parent).get
                    logger.debug(s"Processing $parent -> $nd, parentSlack = $parentSlack")
                    val newEdges = if (parentSlack < 0) {
                        logger.debug(s"Adding $parent -> $nd")
                        (nd -> parent) :: (parent -> nd) :: edges
                    } else edges
                    val newSlacks = if (slacks(parent) > parentSlack)
                        slacks.updated(parent, parentSlack)
                    else
                        slacks
                    val next = if (slacks(parent) == Double.PositiveInfinity && !tree.withinStartNeighbourhood.contains(parent) && tree.parentMap.contains(parent))
                        dequeued.enqueue(parent)
                    else
                        dequeued
                    processQueue(next, newEdges, newSlacks)
                }
            }

            val leaves = tree.parentMap.keys.filterNot(tree.childMap.contains).toSeq
            val slacks = leaves.map(x => x -> neighSizes.neighbourhoodSize(x)).toMap.withDefaultValue(Double.PositiveInfinity)
            processQueue(Queue(leaves: _*), Nil, slacks)
        }
    }
}