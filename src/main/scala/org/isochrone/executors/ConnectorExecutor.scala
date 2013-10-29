package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.connect.GraphConnectorComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.db.HigherConfigRoadNetTableComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.OptionParserComponent
import org.isochrone.ActionComponent
import org.isochrone.osm.DefaultCostAssignerComponent

trait ConnectorExecutor extends ActionExecutor {
    self: Main.type =>

    abstract override def actions = super.actions + (
            "connect" --> new GraphConnectorComponent
                             with OptionsBase
                             with ActionComponent
                             with ConfigRoadNetTableComponent 
                             with HigherConfigRoadNetTableComponent 
                             with FromOptionDatabaseComponent 
                             with OptionParserComponent
                             with DefaultCostAssignerComponent { 
                val execute = () => GraphConnector.connectGraph()
            })
}