package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.ActionComponent
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.OptionParserComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.db.ConfigRegularPartitionComponent
import org.isochrone.partition.QuickPartitionComponent

trait QuickPartitionExecutor extends ActionExecutor {
    self: Main.type =>

    abstract override def actions = super.actions + ("quickpart" -->
        new ActionComponent
        with OptionParserComponent
        with OptionsBase 
        with FromOptionDatabaseComponent
        with ConfigRoadNetTableComponent
        with ConfigRegularPartitionComponent 
        with QuickPartitionComponent {
            val execute = Partitioner.partition _
        })

}