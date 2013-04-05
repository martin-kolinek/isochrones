package org.isochrone.partition.merging

import org.isochrone.graphlib._
import scala.collection.Set

/**
 * mergePriority should be comutative
 */
class Partition[T:HasNeighbours] private (val nodesToCells: Map[T, Cell[T]], mergePriority:(Cell[T], Cell[T])=>Double) {
    lazy val cells = nodesToCells.values.toSet

    lazy val size = nodesToCells.size

    lazy val boundaryEdges = 
        for {
            c1 <- nodesToCells.values
            (src, dst) <- c1.leaving
            c2 = nodesToCells(dst)
        } yield (c1, c2, src, dst)

    lazy val neighbouring:Set[Set[Cell[T]]] = 
        boundaryEdges.map(x => Set(x._1, x._2)).toSet
        
    lazy val toMerge = neighbouring.view.map(_.toSeq).map{
        case Seq(c1, c2) => (mergePriority(c1, c2), c1, c2)
    }.maxBy(_._1)

    def next = 
      if(neighbouring.size == 0) None
      else Some(nextPartition)

    private def nextPartition = {
        val (_, c1, c2) = toMerge
        val merged = c1++c2
        val newNodesToCells = nodesToCells.map {
            case (n, c) if c==c1 || c==c2 => n -> merged
            case x => x
        }
        new Partition(newNodesToCells, mergePriority)
    }
    override def toString = s"Partition($cells)"
}

object Partition {
    def apply[T:HasNeighbours](nodes:Traversable[T], mergePriority:(Cell[T], Cell[T])=>Double) = 
        new Partition[T](nodes.map(x => x->Cell(x)).toMap, mergePriority)
}
