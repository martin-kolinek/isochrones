package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.ActionComponent
import org.isochrone.Main.OptionsBase
import org.isochrone.Main
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.osm.RoadImporterComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.osm.DefaultCostAssignerComponent
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.osm.DuplicitRemoverComponent

trait RoadImportExecutor extends ActionExecutor with Logging {
    self: Main.type =>

    abstract override def actions = super.actions + ("roadimport" --> new ActionComponent with OptionsBase with FromOptionDatabaseComponent with RoadImporterComponent with OsmTableComponent with DefaultRoadNetTableComponent with DefaultCostAssignerComponent with DuplicitRemoverComponent {
        val execute = () => {
            logger.info("Importing roads")
            roadImporter.execute()
            logger.info("Removing duplicit edges")
            DuplicitRemover.removeDupEdges()
            logger.info("Removing duplicit nodes")
            DuplicitRemover.removeDupNodes()
        }
    })
}