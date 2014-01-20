package org.isochrone.visualize

import org.isochrone.compute.IsochroneComputerComponentTypes
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.graphlib.GraphComponentBase

trait AreaVisualizerComponentTypes extends IsochroneComputerComponentTypes {
    self: GraphComponentBase =>

    trait AreaVisualizer {
        def areaGeom(arid: Long, nodes: List[IsochroneNode]): Option[Geometry]
    }
}

trait AreaVisualizerComponent extends AreaVisualizerComponentTypes {
    self: GraphComponentBase =>
    val areaVisualizer: AreaVisualizer
}