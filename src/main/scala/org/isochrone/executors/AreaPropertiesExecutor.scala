package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.ActionComponent
import org.isochrone.areas.AreaCoverCostComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.OptionParserComponent
import org.isochrone.areas.DbAreaReaderComponent
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.areas.AreaPropertiesFinderComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.osm.DefaultCostAssignerComponent

trait AllAreaCoverCostsExecutor extends ActionExecutor {
	self: Main.type =>
	    
	abstract override def actions = super.actions + ("areaprops" --> new ActionComponent
	    with OptionsBase
	    with AreaPropertiesFinderComponent
	    with DefaultCostAssignerComponent
	    with AreaCoverCostComponent
	    with ConfigRoadNetTableComponent
	    with FromOptionDatabaseComponent
	    with OptionParserComponent 
	    with DbAreaReaderComponent
	    with DijkstraAlgorithmProviderComponent
	    with SingleSessionProvider
	    with GraphComponentBase {
	        override type NodeType = Long
	        val reader = new DbAreaReader {}
	    	val execute = () => AreaPropertiesSaver.saveAreaProperties()
	    })
}