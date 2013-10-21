package org.isochrone.executors
import scala.slick.driver.BasicDriver.simple._
import org.isochrone.graphlib._
import org.isochrone.util.Timing._
import org.isochrone.ActionComponent
import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.output.GeometryOutputComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.compute.IsochronesComputationComponent
import org.isochrone.compute.PointIsochroneOutputComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.DatabaseOptionParsingComponent
import org.isochrone.dbgraph.DatabaseGraphComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.db.SingleSessionProvider
import scopt.Read
import org.isochrone.OptionParserComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.dijkstra.MultiLevelDijkstraComponent
import org.isochrone.dbgraph.ConfigDatabaseGraphComponent
import org.isochrone.dbgraph.ConfigMultiLevelDatabaseGraph
import org.isochrone.db.ConfigMultiLevelRoadNetTableComponent
import org.isochrone.ArgumentParser

trait DijkstraIsochroneComputer extends ActionExecutor {
    self: Main.type =>
    abstract override def actions = {
        super.actions + ("dijkstra" --> new ActionComponent 
        		with IsochroneExecutorCompoent
        		with OptionsBase
        		with ConfigRoadNetTableComponent
        		with FromOptionDatabaseComponent
        		with ConfigDatabaseGraphComponent
        		with SingleSessionProvider
        		with DijkstraAlgorithmComponent 
        		with OptionParserComponent
        		with GraphComponentBaseWithDefault {
            def readNodeType = implicitly[Read[NodeType]]
            def noNode = 0l
        }) + ("multidijkstra" --> new ActionComponent
                with IsochroneExecutorCompoent
                with OptionsBase
                with MultiLevelDijkstraComponent
                with ConfigMultiLevelRoadNetTableComponent
                with ConfigMultiLevelDatabaseGraph
                with SingleSessionProvider
                with FromOptionDatabaseComponent
                with GraphComponentBaseWithDefault
                with OptionParserComponent {
            def readNodeType = implicitly[Read[NodeType]]
            def noNode = 0l
        })
    }
}
