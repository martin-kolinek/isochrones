package org.isochrone.partition.merging

import org.isochrone.graphlib._
import scala.collection.immutable.Map
import scala.collection.immutable.SortedMap
import scala.collection.immutable.Set
import org.isochrone.util.collection.immutable.IndexedPriorityQueue

class Partition[T:HasNeighbours] private (
		val cellNeighbours:Map[Cell[T], Set [Cell[T]]],
		val priorities:IndexedPriorityQueue[Set[Cell[T]], Double],
		val mergePriority:(Cell[T], Cell[T])=>Double,
		val nodes:Int,
		val boundaryEdges:Map[Set[Cell[T]], Int],
		val boundaryEdgeCount:Int) {}
object Partition {
	/**
	 * mergePriority should be comutative
	 */
    def apply[T:HasNeighbours](nodes:Traversable[T], mergePriority:(Cell[T], Cell[T])=>Double) = {
    	val cells = nodes.map(x=>x->Cell(x)).toMap
    	val cellNeighbours = cells.map(x=>x._2 -> x._1.neighbours.map(y=>cells(y._1)).toSet).toMap
    	val priorities = for {
    		(c1, n1) <- cellNeighbours
    		c2 <- n1
    	} yield Set(c1, c2) -> mergePriority(c1, c2)
    	val priorityQueue = IndexedPriorityQueue(priorities.toSeq:_*)
    	val boundaryEdges = priorityQueue.map(_ -> 1).toMap
    	val boundaryEdgeCount = boundaryEdges.values.sum
    	new Partition[T](cellNeighbours, priorityQueue, mergePriority, nodes.size, boundaryEdges, boundaryEdgeCount)
    } 
    
    def step[T:HasNeighbours](p:Partition[T]):Partition[T] = {
    	import p._
    	if(priorities.size == 0)
    		p
    	else {
    		val cls@Seq(c1, c2) = priorities.maximum.toSeq
    		val merged = c1 ++ c2
    		val neighboursToRemove = for{
    			c<-cls
    			n<-cellNeighbours(c)
    		} yield (n, c)
    	
    		val newCellNeighbours = 
    		    (cellNeighbours /: neighboursToRemove)((x, y) => x.updated(y._1, x(y._1) - y._2 + merged)) -   
    	        c1 - 
    	        c2 + 
    	        (merged -> neighboursToRemove.map(_._1).filter(x=>x!=c1 && x!=c2).toSet)
    	    val neighToRemSets = neighboursToRemove.map(x=>Set(x._1, x._2))
    	    val newPriorities = priorities -- neighToRemSets +++ 
    	        neighboursToRemove.filter(_._1!=c1).filter(_._1!=c2).
    	        map(_._1 -> merged).map(x=> Set(x._1, x._2)->mergePriority.tupled(x))
    	    val newBoundEdgCount = boundaryEdgeCount - boundaryEdges(Set(c1, c2))
    	    val newBoundaryEdges = boundaryEdges -- neighToRemSets ++
    	        neighboursToRemove.
    	        groupBy(_._1).map(x=>Set(merged, x._1) -> x._2.map(y=>boundaryEdges(Set(y._1, y._2))).sum)
    	    new Partition[T](newCellNeighbours, newPriorities, mergePriority, nodes, newBoundaryEdges, newBoundEdgCount)
    	}
    }
        
}
