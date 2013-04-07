package org.isochrone.partition

import org.isochrone.graphlib._

package object merging {
    /* use merging algorithm to find the partition with maximum value */
    def partition[T:HasNeighbours](nodes:Traversable[T], 
                                   mergePriority:(Cell[T], Cell[T])=>Double, 
                                   partitionValue:Partition[T]=>Double,
                                   stepNotification:Int => Unit = x=>Unit) = {
        val initial = Partition(nodes, mergePriority)
        val it = Iterator.iterate(initial)(part => { 
        	stepNotification(part.cellNeighbours.size)
        	Partition.step(part)
        })
        val best = it.takeWhile(_.cellNeighbours.size>1).maxBy(partitionValue)
        best.cellNeighbours.keys.map(_.nodes)
    }
}
