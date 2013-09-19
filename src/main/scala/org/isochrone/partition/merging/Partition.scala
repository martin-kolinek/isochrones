package org.isochrone.partition.merging

import org.isochrone.graphlib._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.immutable.Set
import scala.collection.mutable.HashSet
import org.isochrone.util.collection.mutable.IndexedPriorityQueue

trait PartitionComponent {
	self: CellComponent with GraphComponent with MergingAlgorithmPropertiesComponent =>
    
    class Partition private (
            var cells: Set[Cell],
            val cellNeighbours: Map[Cell, Set[Cell]],
            val priorities: IndexedPriorityQueue[Set[Cell], Double],
            val nodes: Int,
            val boundaryEdges: Map[Set[Cell], Int],
            var boundaryEdgeCount: Int) {
        def step() {
            if (priorities.empty)
                return
            val Seq(c1, c2) = priorities.maximum._1.toSeq
            val merged = c1 ++ c2
            cells = cells - c1 - c2 + merged

            val neighPairs = for {
                c <- Seq(c1, c2)
                cn <- cellNeighbours(c)
            } yield Set(c, cn)

            val neighbourCellsOfMerged = neighPairs.map(_ - c1 - c2).filter(!_.isEmpty).map(_.head).toSet

            //cellNeigbhours
            val mergedNeighbours = cellNeighbours(c1) - c2 ++ cellNeighbours(c2) - c1
            cellNeighbours -= c1 -= c2
            if (!mergedNeighbours.isEmpty)
                cellNeighbours(merged) = mergedNeighbours
            for (neigh <- neighbourCellsOfMerged) {
                cellNeighbours(neigh) = cellNeighbours(neigh) - c1 - c2 + merged
            }

            //priorities
            priorities --= neighPairs
            val newPairs = neighbourCellsOfMerged.zip(Stream.continually(merged))
            priorities ++= newPairs.map(x => Set(x._1, x._2) -> (mergeAlgProps.mergePriorityFunc _).tupled(x))

            //boundaryEdges
            boundaryEdgeCount -= boundaryEdges(Set(c1, c2))
            val neighToEdgeCount = for {
                cs <- neighPairs
                be = boundaryEdges(cs)
                csWithoutMerged = cs - c1 - c2
                if !csWithoutMerged.isEmpty
                neighbourCell = csWithoutMerged.head
            } yield neighbourCell -> be
            boundaryEdges ++= neighToEdgeCount.groupBy(_._1).map(x => Set(merged, x._1) -> x._2.map(_._2).sum)
            boundaryEdges --= neighPairs
        }
    }

    object Partition {
        def apply(nodes: Traversable[NodeType]) = {
            val cells = nodes.map(x => x -> Cell(x)).toMap
            val cellNeighbours = HashMap(cells.map(x => x._2 -> graph.neighbours(x._1).map(y => cells(y._1)).toSet).toSeq: _*)
            val priorities = for {
                (c1, n1) <- cellNeighbours
                c2 <- n1
            } yield Set(c1, c2) -> mergeAlgProps.mergePriorityFunc(c1, c2)
            val priorityQueue = IndexedPriorityQueue(priorities.toSeq: _*)
            val boundaryEdges = HashMap(priorityQueue.map(_._1 -> 1).toSeq: _*)
            val boundaryEdgeCount = boundaryEdges.values.sum
            new Partition(cells.values.toSet, cellNeighbours, priorityQueue, nodes.size, boundaryEdges, boundaryEdgeCount)
        }
    }

}