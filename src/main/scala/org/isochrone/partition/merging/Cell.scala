package org.isochrone.partition.merging

import org.isochrone.graphlib._
import scala.collection.immutable.Set
/*
class Cell[T:HasNeighbours] private (val nodes:Set[T], val leaving:Set[(T, T)]) {
    def ++ (other:Cell[T]) = 
        new Cell(nodes ++ other.nodes, 
                 leaving.filter(x => !other.nodes.contains(x._2)) ++ other.leaving.filter(x => !nodes.contains(x._2)))
    lazy val boundarySize = leaving.map(_._1).toSet.size
    lazy val size = nodes.size
    lazy val edges = nodes.flatMap(_.neighbours).filter(x => nodes.contains(x._1)).size
    
    override def toString = s"Cell($nodes)"
}

object Cell {
    def apply[T:HasNeighbours](nd:T) = new Cell[T](Set(nd), nd.neighbours.map(nd -> _._1).toSet)
}
*/