package org.isochrone.executors

import org.isochrone.connect.WalkingEdgesAdderComponent
import org.isochrone.Main
import org.isochrone.ActionExecutor
import org.isochrone.ActionComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.connect.SimpleWalkingEdgesAdderComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.osm.DefaultCostAssignerComponent
import org.isochrone.connect.ConfigMaxCostQuotientComponent
import org.isochrone.dbgraph.ConfigDatabaseGraphComponent

trait WalkingEdgesAdderExecutor extends ActionExecutor {
    self: Main.type =>
    abstract override def actions = super.actions +
        ("walking" --> new ActionComponent
                with OptionsBase
                with SimpleWalkingEdgesAdderComponent 
                with FromOptionDatabaseComponent
                with SingleSessionProvider 
                with ConfigRoadNetTableComponent 
                with DijkstraAlgorithmComponent
                with ConfigDatabaseGraphComponent
                with DefaultCostAssignerComponent 
                with ConfigMaxCostQuotientComponent {
        	override type NodeType = Long
        	val execute = () => addWalkingEdges()
        })

}