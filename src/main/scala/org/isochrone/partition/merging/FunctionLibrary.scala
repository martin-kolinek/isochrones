package org.isochrone.partition.merging
import org.isochrone.graphlib._
import scala.math
import scala.util.Random

object FunctionLibrary {
    def mergePriority[T:HasNeighbours](c1:Cell[T], c2:Cell[T]) = {
        val connecting = c1.leaving.toSeq.map(_._2).filter(c2.nodes.contains(_)).size
        val newBoundary = (c1++c2).boundarySize
        val rand = 1.0
        connecting*(1.0 + c1.boundarySize + c2.boundarySize - newBoundary) / (c1.size*c2.size)
    }
    
    def randMergePriority[T:HasNeighbours](c1:Cell[T], c2:Cell[T]) = {
    	(Random.nextDouble/100.0+1.0)*mergePriority(c1, c2)
    }
    
    def boundaryEdgesCellSize[T](size:Int) = (p:Partition[T]) => {
    	cellSize(size)(p) - p.boundaryEdgeCount.toDouble
    }
    
    def cellSize[T](size:Int) = (p:Partition[T]) => -p.cells.toSeq.map{x=>
        val rat = x.size.toDouble/size
        (rat*rat+1.0)/rat - 2.0
    }.map(x=>x*x).sum
}
