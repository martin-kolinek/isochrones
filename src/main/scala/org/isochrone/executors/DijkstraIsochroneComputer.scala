package org.isochrone.executors
import scala.slick.driver.BasicDriver.simple._
import org.isochrone.graphlib._
import org.isochrone.util.Timing._
import org.isochrone.ActionComponent
import org.isochrone.ActionExecutor
import org.isochrone.Main

trait DijkstraIsochroneComputer extends ActionExecutor {
    self: Main.type =>
    abstract override def actions = {
        super.actions + ("dijkstra" -> new ActionComponent {
            val execute = () => println("dijkstra")
        })
    }
}
