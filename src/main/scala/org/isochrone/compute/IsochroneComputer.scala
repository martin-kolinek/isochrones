package org.isochrone.compute

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.ArgumentParser

trait IsochroneComputerComponent {
    self: GraphComponentBase =>

    case class IsochroneEdge(start: NodeType, end: NodeType, part: Double)

    trait IsochroneComputer {
        def isochrone(start: Traversable[(NodeType, Double)], max: Double): Traversable[IsochroneEdge]
    }
}

trait SomeIsochroneComputerComponent extends IsochroneComputerComponent {
    self: GraphComponentBase =>
    val isoComputer: IsochroneComputer
}

trait IsochronesComputationComponent {
    self: IsochroneParamsParsingComponent with SomeIsochroneComputerComponent with ArgumentParser =>

    lazy val isochrone = isoComputer.isochrone(List(parsedConfig.start -> 0.0), parsedConfig.limit)
}