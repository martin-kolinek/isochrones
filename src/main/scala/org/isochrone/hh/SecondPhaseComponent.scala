package org.isochrone.hh

import scala.collection.immutable.Queue
import scala.annotation.tailrec
import org.isochrone.graphlib.GraphComponent

trait SecondPhaseComponent {
    self: GraphComponent with NeighbourhoodSizeComponent with FirstPhaseComponent =>

    object SecondPhase {
        def extractHighwayEdges(tree: NodeTree) = {
            @tailrec
            def processQueue(q: Queue[NodeType], edges: List[(NodeType, NodeType)], slacks: Map[NodeType, Double]): List[(NodeType, NodeType)] = {
                if (q.isEmpty)
                    edges
                else {
                    val (nd, dequeued) = q.dequeue
                    val parent = tree.parentMap(nd)
                    val parentSlack = slacks(nd) - graph.edgeCost(nd, parent).get
                    val newEdges = if (parentSlack < 0)
                        (nd -> parent) :: (parent -> nd) :: edges
                    else edges
                    val newSlacks = if (slacks(parent) > parentSlack)
                        slacks.updated(parent, parentSlack)
                    else
                        slacks
                    val next = if (slacks(parent) == Double.PositiveInfinity && !tree.withinStartNeighbourhood.contains(parent))
                        dequeued.enqueue(parent)
                    else
                        dequeued
                    processQueue(next, newEdges, newSlacks)
                }
            }

            val leaves = tree.parentMap.keys.filterNot(tree.childMap.contains).toSeq

        }
    }
}