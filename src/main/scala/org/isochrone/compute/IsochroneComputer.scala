package org.isochrone.compute

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphComponentBaseWithDefault

trait IsochroneComputerComponentTypes {
    self: GraphComponentBase =>
    case class IsochroneEdge(start: NodeType, end: NodeType, part: Double)
}

trait IsochroneComputerComponent extends IsochroneComputerComponentTypes {
    self: GraphComponentBase =>

    trait IsochroneComputer {
        def isochrone(start: Traversable[(NodeType, Double)], max: Double): Traversable[IsochroneEdge]
    }
}

trait SomeIsochroneComputerComponent extends IsochroneComputerComponent {
    self: GraphComponentBase =>
    val isoComputer: IsochroneComputer
}

trait IsochronesComputationComponent extends IsochroneComputerComponentTypes {
    self: GraphComponentBase =>
    def isochrone: Traversable[IsochroneEdge]
}

trait DefaultIsochronesComputationComponent extends IsochronesComputationComponent with IsochroneParamsParsingComponent {
    self: SomeIsochroneComputerComponent with ArgumentParser with GraphComponentBaseWithDefault =>

    lazy val isochrone = isoComputer.isochrone(List(startNodeLens.get(parsedConfig) -> 0.0), limitLens.get(parsedConfig))
}