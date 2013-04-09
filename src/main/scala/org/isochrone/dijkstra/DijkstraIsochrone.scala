package org.isochrone.dijkstra

import org.isochrone.graphlib._
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.PriorityQueue
import scala.collection.mutable.TreeSet
import org.isochrone.util._
import org.isochrone.util.collection.mutable.IndexedPriorityQueue

object DijkstraIsochrone {

	def computeIsochrone[T:HasNeighbours](start:Traversable[(T, Double)], max:Double, res:(T, Double)=>Unit) {
        
		val closed = new HashSet[T]
		val costMap = new HashMap[T, Double]
        val open = IndexedPriorityQueue(start.toSeq:_*)
		costMap ++= start
		
		while(!open.empty) {
			val (current, curCost) = open.minimum
            open -= current
			closed += current
            if(curCost>max)
                return
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
	
	implicit def tHasComputableIsochrone[T:HasNeighbours:Ordering](implicit prec:DoublePrecision) = new HasComputableIsochrone[T]{
		def isochrone(start:Traversable[(T, Double)], limit:Double, res:(T, Double)=>Unit) = computeIsochrone(start, limit, res)
	}
}
