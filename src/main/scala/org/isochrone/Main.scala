package org.isochrone

import org.isochrone.db.OnlyDatabaseParserComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.osm.TableCreatorComponent
import org.isochrone.db.VisualizationTableComponent
import org.isochrone.db.DefaultVisualizationTableComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.osm.RoadImporterComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.OnlyDatabaseParserComponent
import org.isochrone.osm.RoadNetVisualizerComponent
import org.isochrone.osm.DefaultCostAssignerComponent
import org.isochrone.executors.DijkstraIsochroneComputer
import org.isochrone.executors.HigherLevelCreator
import org.isochrone.executors.RoadImportExecutor
import org.isochrone.executors.RoadVisualizeExecutor
import org.isochrone.executors.SchemaExecutor

object Main extends App
        with DijkstraIsochroneComputer
        with HigherLevelCreator
        with RoadImportExecutor
        with RoadVisualizeExecutor
        with SchemaExecutor {
    trait OptionsBase extends DefaultArgumentParser with ArgumentsProvider {
        self: OptionParserComponent =>
        def arguments = args.tail
    }

    val act = args.headOption.getOrElse("")
    val acts = actions
    if (!acts.contains(act)) {
        println(s"Action $act not understood, possible actions:")
        acts.keys.foreach(println)
    } else
        acts(act).execute()
}