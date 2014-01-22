package org.isochrone.visualize

import org.isochrone.compute.IsochroneComputerComponentTypes
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.areas.PosAreaComponent

trait AreaVisualizerComponentTypes extends IsochroneComputerComponentTypes with PosAreaComponent {
    self: GraphComponentBase =>

    trait AreaVisualizer {
        def areaGeom(area: PosArea, areaGeom: Geometry, nodes: List[IsochroneNode]): Option[Geometry]
    }
}

trait AreaVisualizerComponent extends AreaVisualizerComponentTypes {
    self: GraphComponentBase =>
    val areaVisualizer: AreaVisualizer
}