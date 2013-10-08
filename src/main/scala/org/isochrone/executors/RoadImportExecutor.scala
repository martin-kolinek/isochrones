package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.ActionComponent
import org.isochrone.Main.OptionsBase
import org.isochrone.Main
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.OnlyDatabaseParserComponent
import org.isochrone.osm.RoadImporterComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.osm.DefaultCostAssignerComponent

trait RoadImportExecutor extends ActionExecutor {
    self: Main.type =>

    abstract override def actions = super.actions + ("roadimport" -> new ActionComponent with OptionsBase with FromOptionDatabaseComponent with OnlyDatabaseParserComponent with RoadImporterComponent with OsmTableComponent with DefaultRoadNetTableComponent with DefaultCostAssignerComponent {
        val execute = roadImporter.execute _
    })
}