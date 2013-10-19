package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.ActionComponent
import org.isochrone.partition.IncrementalPartitionComponent
import org.isochrone.partition.merging.DefaultMergingPartitionerProvider
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.Main
import org.isochrone.db.ConfigRegularPartitionComponent
import org.isochrone.partition.ConfigIncrementalPartitionComponent
import org.isochrone.dboutput.partition.HigherLevelGraphCreatorComponent
import org.isochrone.dbgraph.DatabaseGraphComponent
import org.isochrone.db.HigherConfigRoadNetTableComponent
import org.isochrone.partition.DefaultRegionAnalyzerProvider
import org.isochrone.db.SingleSessionProvider
import org.isochrone.partition.merging.ConfigMergingPartitionerProvider
import org.isochrone.OptionParserComponent

trait HigherLevelCreator extends ActionExecutor {
    self: Main.type =>
	abstract override def actions = super.actions + ("partition" --> new ActionComponent
	        with OptionsBase
	        with FromOptionDatabaseComponent
	        with ConfigIncrementalPartitionComponent 
	        with ConfigMergingPartitionerProvider
	        with ConfigRoadNetTableComponent
	        with ConfigRegularPartitionComponent 
	        with OptionParserComponent {
	    val execute = () => {
	        partitioner.partition()
	        
	    }
	}) + ("higher" --> new ActionComponent
	        with OptionsBase
	        with HigherLevelGraphCreatorComponent 
	        with ConfigRoadNetTableComponent
	        with DatabaseGraphComponent
	        with HigherConfigRoadNetTableComponent
	        with DefaultRegionAnalyzerProvider
	        with FromOptionDatabaseComponent
	        with SingleSessionProvider {
	    val execute = () => HigherLevelGraph.createHigherLevelGraph()
	})
}