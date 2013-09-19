package org.isochrone.partition.merging

import org.isochrone.graphlib._

trait MergingPartitionerComponent {
	self:GraphComponent with PartitionComponent with CellComponent with MergingAlgorithmPropertiesComponent =>
	    
    object MergingPartitioner {
        /* use merging algorithm to find the partition with maximum value */
        def partition(stepNotification: (Int, Double) => Unit = (x, y) => Unit) = {
            val part = Partition(graph.nodes)
            val trav = new Traversable[(Double, Set[Cell])] {
                def foreach[U](func: ((Double, Set[Cell])) => U) {
                    var lastSize = 0
                    while (lastSize != part.cells.size) {
                        val value = mergeAlgProps.partitionValueFunc(part)
                        func((value, part.cells))
                        lastSize = part.cells.size
                        stepNotification(lastSize, value)
                        part.step();
                    }
                }
            }
            trav.maxBy(_._1)._2.map(_.nodes)
        }
    }
}
