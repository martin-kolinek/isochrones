package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.ActionComponent
import org.isochrone.areas.AreaCoverCostComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.OptionParserComponent
import org.isochrone.areas.DbAreaReaderComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.db.SingleSessionProvider
import org.isochrone.areas.AreaPropertiesFinderComponent
import org.isochrone.graphlib.GraphComponentBase

trait AllAreaCoverCostsExecutor extends ActionExecutor {
	self: Main.type =>
	    
	abstract override def actions = super.actions + ("areaprops" --> new ActionComponent
	    with OptionsBase
	    with AreaPropertiesFinderComponent
	    with AreaCoverCostComponent
	    with ConfigRoadNetTableComponent
	    with FromOptionDatabaseComponent
	    with OptionParserComponent 
	    with DbAreaReaderComponent
	    with DefaultDijkstraProvider
	    with SingleSessionProvider
	    with GraphComponentBase {
	        override type NodeType = Long
	        val reader = new DbAreaReader {}
	    	val execute = () => AreaPropertiesSaver.saveAreaProperties()
	    })
}