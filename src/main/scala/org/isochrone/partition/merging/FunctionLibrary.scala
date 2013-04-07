package org.isochrone.partition.merging
import org.isochrone.graphlib._
import scala.math

object FunctionLibrary {
    def mergePriority[T:HasNeighbours](c1:Cell[T], c2:Cell[T]) = {
        val connecting = c1.leaving.map(_._2).filter(c2.nodes.contains(_)).size
        val newBoundary = (c1++c2).boundarySize
        val rand = 1.0
        rand*connecting*(1 + c1.boundarySize + c2.boundarySize - newBoundary) / (c1.size*c2.size)
    }

    def negAvgSearchGraphSize[T:HasNeighbours](p:Partition[T]) = {
        val cellCosts = for(c<-p.cellNeighbours.keys) 
            yield c.size.toDouble/p.nodes * (2.0 - c.size.toDouble/p.nodes) * c.edges + math.pow((1.0 - c.size.toDouble/p.nodes),2.0) * math.pow(c.size,2.0)
        -cellCosts.sum - p.boundaryEdgeCount
    }
}
