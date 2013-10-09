package org.isochrone.compute

import org.isochrone.graphlib.NodePositionComponent

trait IsochroneOutputComponent {
    self: NodePositionComponent with IsochroneComputerComponent =>
}