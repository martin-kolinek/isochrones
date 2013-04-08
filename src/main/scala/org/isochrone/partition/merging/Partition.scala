package org.isochrone.partition.merging

import org.isochrone.graphlib._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.immutable.Set
import scala.collection.mutable.HashSet
import org.isochrone.util.collection.mutable.IndexedPriorityQueue

class Partition[T:HasNeighbours] private (
        var cells:Set[Cell[T]],
		val cellNeighbours:Map[Cell[T], Set[Cell[T]]],
		val priorities:IndexedPriorityQueue[Set[Cell[T]], Double],
		val mergePriority:(Cell[T], Cell[T])=>Double,
		val nodes:Int,
		val boundaryEdges:Map[Set[Cell[T]], Int],
		var boundaryEdgeCount:Int) {
    def step() {
    	if(priorities.empty)
    		return
        val Seq(c1, c2) = priorities.maximum.toSeq
        val merged = c1 ++ c2
        cells = cells - c1 - c2 + merged
        
        val neighPairs = for {
        	c <- Seq(c1, c2)
        	cn <- cellNeighbours(c)
        } yield Set(c, cn)
        
        val neighbourCellsOfMerged = neighPairs.map(_-c1-c2).filter(!_.isEmpty).map(_.head).toSet 
        
        //cellNeigbhours
        val mergedNeighbours = cellNeighbours(c1) - c2 ++ cellNeighbours(c2) - c1
        cellNeighbours -= c1 -= c2
        if(!mergedNeighbours.isEmpty)
        cellNeighbours(merged) = mergedNeighbours
        for(neigh <- neighbourCellsOfMerged) {
        	cellNeighbours(neigh) = cellNeighbours(neigh) - c1 - c2 + merged
        }
        
        //priorities
        priorities --= neighPairs
        val newPairs = neighbourCellsOfMerged.zip(Stream.continually(merged))
        priorities ++= newPairs.map(x=>Set(x._1, x._2) -> mergePriority.tupled(x))
        
        //boundaryEdges
        boundaryEdgeCount -= boundaryEdges(Set(c1, c2))
        val neighToEdgeCount = for {
        	cs <- neighPairs
        	be = boundaryEdges(cs)
        	csWithoutMerged = cs - c1 - c2
        	if !csWithoutMerged.isEmpty
        	neighbourCell = csWithoutMerged.head
        } yield neighbourCell -> be
        boundaryEdges ++= neighToEdgeCount.groupBy(_._1).map(x=>Set(merged, x._1) -> x._2.map(_._2).sum)
        boundaryEdges --= neighPairs
    }
}
object Partition {
	/**
	 * mergePriority should be comutative
	 */
    def apply[T:HasNeighbours](nodes:Traversable[T], mergePriority:(Cell[T], Cell[T])=>Double) = {
    	val cells = nodes.map(x=>x->Cell(x)).toMap
    	val cellNeighbours = HashMap(cells.map(x=>x._2 -> x._1.neighbours.map(y=>cells(y._1)).toSet).toSeq:_*)
    	val priorities = for {
    		(c1, n1) <- cellNeighbours
    		c2 <- n1
    	} yield Set(c1, c2) -> mergePriority(c1, c2)
    	val priorityQueue = IndexedPriorityQueue(priorities.toSeq:_*)
    	val boundaryEdges = HashMap(priorityQueue.map(_._1 -> 1).toSeq:_*)
    	val boundaryEdgeCount = boundaryEdges.values.sum
    	new Partition[T](cells.values.toSet, cellNeighbours, priorityQueue, mergePriority, nodes.size, boundaryEdges, boundaryEdgeCount)
    } 
}
