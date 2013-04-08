package org.isochrone.partition

import org.isochrone.graphlib._

package object merging {
    /* use merging algorithm to find the partition with maximum value */
    def partition[T:HasNeighbours](nodes:Traversable[T], 
                                   mergePriority:(Cell[T], Cell[T])=>Double, 
                                   partitionValue:Partition[T]=>Double,
                                   stepNotification:Int => Unit = x=>Unit) = {
        val part = Partition(nodes, mergePriority)
        val trav = new Traversable[(Double, Set[Set[T]])] {
            def foreach[U](func:((Double, Set[Set[T]])) => U) {
                while(true) {
                    func((partitionValue(part), part.cells.map(_.nodes).toSet))
                    part.step();
                }
            }
        }
        trav.takeWhile(_._2.size>1).maxBy(_._1)._2
    }
}
