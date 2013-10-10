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

trait DijkstraIsochroneComputer extends ActionExecutor {
    self: Main.type =>
    abstract override def actions = {
        super.actions + ("dijkstra" -> new ActionComponent 
                /*with GeometryOutputComponent 
                with DijkstraAlgorithmComponent 
                with IsochronesComputationComponent 
                with PointIsochroneOutputComponent*/ {
            val execute = () => println("dijkstra")
        })
    }
}
