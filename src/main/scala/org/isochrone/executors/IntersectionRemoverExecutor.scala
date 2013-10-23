package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.ActionComponent
import org.isochrone.Main
import org.isochrone.OptionParserComponent
import org.isochrone.intersections.IncrementalIntersectionRemoverComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.db.ConfigRegularPartitionComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.osm.DefaultCostAssignerComponent

trait IntersectionRemoverExecutor extends ActionExecutor {
    self: Main.type =>
    abstract override def actions = super.actions +
        ("intersections" --> new ActionComponent 
                with OptionsBase
                with FromOptionDatabaseComponent
                with ConfigRoadNetTableComponent
                with ConfigRegularPartitionComponent
                with OsmTableComponent
                with DefaultCostAssignerComponent
                with IncrementalIntersectionRemoverComponent
                with OptionParserComponent {
        	val execute = () => IncrementalIntersectionRemover.removeIntersections()
        })
}