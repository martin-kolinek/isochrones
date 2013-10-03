package org.isochrone.partition

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.RegularPartitionComponent

trait PartitionerComponent {
    self: GraphComponentBase =>
    trait Partitioner {
        def partition(): Set[Set[NodeType]]
    }

    val partitioner: Partitioner
}

trait BBoxPartitionerProvider {
    self: RegularPartitionComponent =>
    def createPartitioner(bbox: regularPartition.BoundingBox): PartitionerComponent {
        type NodeType = Long
    }
}