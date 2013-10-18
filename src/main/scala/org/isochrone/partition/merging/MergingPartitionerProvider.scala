package org.isochrone.partition.merging

import org.isochrone.db.RegularPartitionComponent
import org.isochrone.dbgraph.WithoutRegionsBoundedGraphComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.graphlib.GraphComponent
import org.isochrone.partition.PartitionerComponent
import org.isochrone.partition.BBoxPartitionerProvider
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser

trait MergingPartitionProviderBase extends WithoutRegionsBoundedGraphComponent with BBoxPartitionerProvider {
    self: RegularPartitionComponent with RoadNetTableComponent with DatabaseProvider =>

    class ResultMergingPartitioner(bbox: regularPartition.BoundingBox) extends GraphComponent with MergingPartitionerComponent with FunctionLibraryComponent {
        inself: MergingAlgorithmPropertiesComponent =>
        type NodeType = self.NodeType
        val graph = WithoutRegionsBoundedGraphCreator.createGraph(bbox)
    }
}

trait DefaultMergingPartitionerProvider extends MergingPartitionProviderBase {
    self: RegularPartitionComponent with RoadNetTableComponent with DatabaseProvider =>

    def createPartitioner(bbox: regularPartition.BoundingBox) =
        new ResultMergingPartitioner(bbox) with DefaultMergingAlgPropertiesComponent
}

trait ConfigMergingPartitionerProvider extends MergingPartitionProviderBase with MergingPropertiesOptionParsingComponent {
    self: RegularPartitionComponent with RoadNetTableComponent with DatabaseProvider with ArgumentParser =>

    def createPartitioner(bbox: regularPartition.BoundingBox) = new ResultMergingPartitioner(bbox) with MergingAlgorithmPropertiesComponent {
        val mergeAlgProps = new MergingAlgorithmProperties {
            def mergePriorityFunc(c1: Cell, c2: Cell) = FunctionLibrary.mergePriority(c1, c2)
            def partitionValueFunc(p: Partition) = FunctionLibrary.cellSize(targetSizeLens.get(parsedConfig))(p)
        }
    }
}

trait MergingPropertiesOptionParsingComponent extends OptionParserComponent {
    lazy val targetSizeLens = registerConfig(70)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Int]("target-size").action((x, c) => targetSizeLens.set(c)(x)).text("the target size of created regions (default = 70)")
    }
}