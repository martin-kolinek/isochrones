package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.areas.AreaSaverComponent
import org.isochrone.dbgraph.ConfigDatabaseGraphComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.ActionComponent
import org.isochrone.OptionParserComponent

trait AreaExecutor extends ActionExecutor {
    self: Main.type =>

        abstract override def actions = super.actions + ("areas" --> 
            new ActionComponent
            with AreaSaverComponent
            with OptionsBase
            with FromOptionDatabaseComponent
            with SingleSessionProvider
            with ConfigRoadNetTableComponent
            with ConfigDatabaseGraphComponent
            with OptionParserComponent {
            	val execute = () => AreaSaver.saveAreas()
            })
}