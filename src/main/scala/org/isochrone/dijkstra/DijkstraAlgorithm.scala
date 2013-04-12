package org.isochrone.dijkstra

import org.isochrone.graphlib._
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.PriorityQueue
import scala.collection.mutable.TreeSet
import org.isochrone.util._
import org.isochrone.util.collection.mutable.IndexedPriorityQueue

object DijkstraAlgorithm {

	private def alg[T:HasNeighbours](start:Traversable[(T, Double)], res:(T, Double)=>Unit) {
		val closed = new HashSet[T]
		val costMap = new HashMap[T, Double]
        val open = IndexedPriorityQueue(start.toSeq:_*)
		costMap ++= start
		
		while(!open.empty) {
			val (current, curCost) = open.minimum
            open -= current
			closed += current
            res(current, curCost)
			for((neighbour, cost) <- current.neighbours if !closed.contains(neighbour)) {
				val newCost = curCost + cost
                val better = costMap.get(neighbour).map(newCost < _)
                if(better.getOrElse(false)) {
                    open -= neighbour
                }
                if(better.getOrElse(true)) {
                    open += neighbour -> newCost
                }
			}
		}
	}
	
	def compute[T:HasNeighbours](start:Traversable[(T, Double)]) = new Traversable[(T, Double)] {
		def foreach[U](func:((T, Double))=>U) {
			alg(start, (x:T, y:Double)=>func(x->y))
		}
	}
	
	def isochrone[T:HasNeighbours](start:Traversable[(T, Double)], max:Double) = 
		compute(start).takeWhile(_._2 <= max)
}
