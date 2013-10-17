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

trait DijkstraIsochroneComputer extends ActionExecutor {
    self: Main.type =>
    abstract override def actions = {
        super.actions + ("dijkstra" -> new ActionComponent 
        		with IsochroneExecutorCompoent
        		with OptionsBase 
        		with FromOptionDatabaseComponent
        		with DatabaseGraphComponent
        		with SingleSessionProvider
        		with DefaultRoadNetTableComponent
        		with DatabaseOptionParsingComponent
        		with DijkstraAlgorithmComponent 
        		with OptionParserComponent
        		with GraphComponentBaseWithDefault {
            def readNodeType = implicitly[Read[NodeType]]
            def noNode = 0l
            val execute = () => writeOutput()
        })
    }
}
