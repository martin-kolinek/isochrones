package org.isochrone.visualize

import org.isochrone.compute.IsochroneOutputComponent
import org.isochrone.compute.IsochronesComputationComponent

trait VisualizationIsochroneOutputComponent extends IsochroneOutputComponent {
    self: IsochronesComputationComponent =>

    def isochroneGeometry = ???
}