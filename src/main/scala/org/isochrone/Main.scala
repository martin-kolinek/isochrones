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
import org.isochrone.executors.AreaFixerExecutor
import org.isochrone.executors.AllAreaCoverCostsExecutor
import org.isochrone.executors.WalkingEdgesAdderExecutor
import org.isochrone.executors.HHCreateExecutor
import org.isochrone.executors.HHStepExecutor
import org.isochrone.executors.AreaDescendLimitExecutor
import org.isochrone.executors.HHQueryExecutor
import org.isochrone.executors.DistanceExecutor
import org.isochrone.executors.QuickPartitionExecutor

object Main extends App with Logging
        with DijkstraIsochroneComputer
        with HigherLevelCreator
        with RoadImportExecutor
        with RoadVisualizeExecutor
        with SchemaExecutor
        with IntersectionRemoverExecutor
        with ConnectorExecutor
        with AreaExecutor
        with AreaFixerExecutor
        with AllAreaCoverCostsExecutor
        with WalkingEdgesAdderExecutor
        with HHCreateExecutor
        with HHStepExecutor
        with AreaDescendLimitExecutor
        with HHQueryExecutor
        with DistanceExecutor
        with QuickPartitionExecutor {

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