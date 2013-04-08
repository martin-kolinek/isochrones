package org.isochrone.partition

import org.isochrone.graphlib._

package object merging {
    /* use merging algorithm to find the partition with maximum value */
    def partition[T:HasNeighbours](nodes:Traversable[T], 
                                   mergePriority:(Cell[T], Cell[T])=>Double, 
                                   partitionValue:Partition[T]=>Double,
                                   stepNotification:Int => Unit = x=>Unit) = {
        val part = Partition(nodes, mergePriority)
        val trav = new Traversable[(Double, Set[Cell[T]])] {
            def foreach[U](func:((Double, Set[Cell[T]])) => U) {
            	var lastSize = 0
                while(lastSize!=part.cells.size) {
                    func((partitionValue(part), part.cells))
                    lastSize = part.cells.size
                    stepNotification(lastSize)
                    part.step();
                }
            }
        }
        trav.maxBy(_._1)._2.map(_.nodes)
    }
}
