package org.isochrone.partition.merging

import org.isochrone.graphlib._
import org.isochrone.partition.PartitionerComponent
import com.typesafe.scalalogging.slf4j.Logging

trait MergingPartitionerComponent extends PartitionComponent with CellComponent with PartitionerComponent {
    self: GraphComponent with MergingAlgorithmPropertiesComponent =>

    object MergingPartitioner extends Partitioner with Logging {
        /* use merging algorithm to find the partition with maximum value */
        def partition() = {
            val part = Partition(graph.nodes)
            val trav = new Traversable[(Double, Set[Cell])] {
                def foreach[U](func: ((Double, Set[Cell])) => U) {
                    var lastSize = 0
                    while (lastSize != part.cells.size) {
                        val value = mergeAlgProps.partitionValueFunc(part)
                        func((value, part.cells))
                        lastSize = part.cells.size
                        if (lastSize % 100 == 0)
                            logger.info(s"partition size: $lastSize")
                        part.step();
                    }
                }
            }
            if (trav.isEmpty)
                Set()
            else
                trav.maxBy(_._1)._2.map(_.nodes)
        }
    }

    val partitioner = MergingPartitioner
}
