package org.isochrone.partition

import org.isochrone.graphlib._

package object merging {
    /* use merging algorithm to find the partition with maximum value */
    def partition[T:HasNeighbours](nodes:Traversable[T], 
                                   mergePriority:(Cell[T], Cell[T])=>Double, 
                                   partitionValue:Partition[T]=>Double,
                                   stepNotification:Int => Unit = x=>Unit) = {
        val initial:Option[Partition[T]] = Some(Partition(nodes, mergePriority))
        val it = Iterator.iterate(initial)(part => part.flatMap{ x=>
        	stepNotification(x.cells.size)
        	x.next
        })
        val best = it.takeWhile(_.isDefined).map(_.get).maxBy(partitionValue(_))
        best.cells.map(_.nodes)
    }
}
