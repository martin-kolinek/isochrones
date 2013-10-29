package org.isochrone

import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.osm.TableCreatorComponent
import org.isochrone.db.VisualizationTableComponent
import org.isochrone.db.DefaultVisualizationTableComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.osm.RoadImporterComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.osm.RoadNetVisualizerComponent
import org.isochrone.osm.DefaultCostAssignerComponent
import org.isochrone.executors.DijkstraIsochroneComputer
import org.isochrone.executors.HigherLevelCreator
import org.isochrone.executors.RoadImportExecutor
import org.isochrone.executors.RoadVisualizeExecutor
import org.isochrone.executors.SchemaExecutor
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.executors.IntersectionRemoverExecutor
import org.isochrone.executors.ConnectorExecutor
import org.isochrone.executors.AreaExecutor

object Main extends App with Logging
        with DijkstraIsochroneComputer
        with HigherLevelCreator
        with RoadImportExecutor
        with RoadVisualizeExecutor
        with SchemaExecutor
        with IntersectionRemoverExecutor
        with ConnectorExecutor
        with AreaExecutor {

    trait OptionsBase extends DefaultArgumentParser with ArgumentsProvider {
        self: OptionParserComponent =>
        def arguments = noProfArgs.tail
    }

    def noProfArgs =
        if (profile) args.tail
        else args

    def profile = args.headOption.getOrElse("") == "prof"

    val act = noProfArgs.headOption.getOrElse("")
    val acts = actions
    if (!acts.contains(act)) {
        println(s"Action $act not understood, possible actions:")
        acts.keys.foreach(println)
    } else {
        if (profile) {
            logger.info(s"Press enter to execute $act")
            readLine()
        }
        logger.info(s"Executing $act")
        acts(act)().execute()
    }
}