package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.db.OnlyDatabaseParserComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.osm.TableCreatorComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.db.DefaultVisualizationTableComponent
import org.isochrone.ActionComponent

trait SchemaExecutor extends ActionExecutor {
    self: Main.type =>

    trait CompleteTableCreatorComponent extends ActionComponent with OnlyDatabaseParserComponent with FromOptionDatabaseComponent with TableCreatorComponent with DefaultRoadNetTableComponent with DefaultVisualizationTableComponent with OptionsBase {
    }

    abstract override def actions = super.actions + ("createdb" -> new CompleteTableCreatorComponent {
        val execute = tableCreator.create _
    }) + ("dropdb" -> new CompleteTableCreatorComponent {
        val execute = tableCreator.drop _
    })
}