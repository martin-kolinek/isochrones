package org.isochrone.compute

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphComponentBaseWithDefault

trait IsochroneComputerComponentTypes {
    self: GraphComponentBase =>
    case class IsochroneNode(nd:NodeType, remaining: Double)
}

trait IsochroneComputerComponent extends IsochroneComputerComponentTypes {
    self: GraphComponentBase =>

    trait IsochroneComputer {
        def isochrone(start: Traversable[(NodeType, Double)], max: Double): Traversable[IsochroneNode]
    }
}

trait SomeIsochroneComputerComponent extends IsochroneComputerComponent {
    self: GraphComponentBase =>
    val isoComputer: IsochroneComputer
}

trait IsochronesComputationComponent extends IsochroneComputerComponentTypes {
    self: GraphComponentBase =>
    def isochrone: Traversable[IsochroneNode]
}

trait DefaultIsochronesComputationComponent extends IsochronesComputationComponent with IsochroneParamsParsingComponent {
    self: SomeIsochroneComputerComponent with ArgumentParser with GraphComponentBaseWithDefault =>

    lazy val isochrone = isoComputer.isochrone(List(startNodeLens.get(parsedConfig) -> 0.0), limitLens.get(parsedConfig))
}