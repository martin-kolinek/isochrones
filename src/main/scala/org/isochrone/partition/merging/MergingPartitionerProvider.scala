package org.isochrone.partition.merging

import org.isochrone.db.RegularPartitionComponent
import org.isochrone.dbgraph.WithoutRegionsBoundedGraphComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.graphlib.GraphComponent
import org.isochrone.partition.PartitionerComponent

trait DefaultMergingPartitionerProvider extends WithoutRegionsBoundedGraphComponent {
    self: RegularPartitionComponent with RoadNetTableComponent with DatabaseProvider =>

    def createPartitioner(bbox: regularPartition.BoundingBox) = new GraphComponent with MergingPartitionerComponent with DefaultMergingAlgPropertiesComponent with FunctionLibraryComponent {
        type NodeType = self.NodeType
        val graph = WithoutRegionsBoundedGraphCreator.createGraph(bbox)
    }
}