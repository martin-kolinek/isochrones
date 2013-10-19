package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.ActionComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.osm.RoadNetVisualizerComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.db.DefaultVisualizationTableComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.ConfigVisualizationTableComponent
import org.isochrone.db.ConfigRoadNetTableComponent

trait RoadVisualizeExecutor extends ActionExecutor {
    self: Main.type =>

    abstract override def actions = super.actions + ("roadvisualize" --> new ActionComponent with OptionsBase with FromOptionDatabaseComponent with RoadNetVisualizerComponent with ConfigRoadNetTableComponent with ConfigVisualizationTableComponent with OsmTableComponent {
        val execute = visualizer.execute _
    })

}