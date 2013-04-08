package org.isochrone.partition.merging

import org.isochrone.graphlib._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.mutable.Set
import scala.collection.mutable.HashSet
import org.isochrone.util.collection.mutable.IndexedPriorityQueue

class Partition[T:HasNeighbours] private (
        val cells:Set[Cell[T]],
		val cellNeighbours:Map[Cell[T], HashSet[Cell[T]]],
		val priorities:IndexedPriorityQueue[Set[Cell[T]], Double],
		val mergePriority:(Cell[T], Cell[T])=>Double,
		val nodes:Int,
		val boundaryEdges:Map[Set[Cell[T]], Int],
		var boundaryEdgeCount:Int) {
    def step() {
        
    }
}
object Partition {
	/**
	 * mergePriority should be comutative
	 */
    def apply[T:HasNeighbours](nodes:Traversable[T], mergePriority:(Cell[T], Cell[T])=>Double) = {
    	val cells = nodes.map(x=>x->Cell(x)).toMap
    	val cellNeighbours = HashMap(cells.map(x=>x._2 -> HashSet(x._1.neighbours.map(y=>cells(y._1)).toSeq:_*)).toSeq:_*)
    	val priorities = for {
    		(c1, n1) <- cellNeighbours
    		c2 <- n1
    	} yield Set(c1, c2) -> mergePriority(c1, c2)
    	val priorityQueue = IndexedPriorityQueue(priorities.toSeq:_*)
    	val boundaryEdges = HashMap(priorityQueue.map(_._1 -> 1).toSeq:_*)
    	val boundaryEdgeCount = boundaryEdges.values.sum
    	new Partition[T](HashSet(cells.values.toSeq:_*), cellNeighbours, priorityQueue, mergePriority, nodes.size, boundaryEdges, boundaryEdgeCount)
    } 
}
