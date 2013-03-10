package org.isochrone.dijkstra

import org.isochrone.graphlib._
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.PriorityQueue
import scala.collection.mutable.TreeSet
import org.isochrone.util._

object DijkstraIsochrone {

	def computeIsochrone[T:HasNeighbours:Ordering](start:Traversable[(T, Double)], max:Double, res:T=>Unit)(implicit prec:DoublePrecision) {
        
		val closed = new HashSet[T]
		val costMap = new HashMap[T, Double]
        import org.isochrone.util.DoubleWithPrecision._
        val open = new TreeSet[(Double, T)] //this uses an implicit conversion from DoubleWithPrecision
		open ++= start.map(_.swap)
		costMap ++= start
		
		while(open.size>0) {
			val cur@(curCost, current) = open.min
            open.remove(cur)
			closed += current
            if(curCost>max)
                return
            res(current)
			for((neighbour, cost) <- current.neighbours if !closed.contains(neighbour)) {
				val newCost = curCost + cost
                val better = costMap.get(neighbour).map(newCost < _)
                if(better.getOrElse(false)) {
                    open.remove((costMap(neighbour), neighbour))
                }
                if(better.getOrElse(true)) {
                    open.add((newCost, neighbour))
                }
			}
		}
	}
	
	implicit def tHasComputableIsochrone[T:HasNeighbours:Ordering](implicit prec:DoublePrecision) = new HasComputableIsochrone[T]{
		def isochrone(start:Traversable[(T, Double)], limit:Double, res:T=>Unit) = computeIsochrone(start, limit, res)
	}
}
