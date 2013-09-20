package org.isochrone.compute

import org.isochrone.graphlib.GraphComponentBase

trait IsochroneComputerComponent {
    self: GraphComponentBase =>

    case class IsochroneEdge(start: NodeType, end: NodeType, part: Double)

    trait IsochroneComputer {
        def isochrone(start:Traversable[(NodeType, Double)], max:Double): Traversable[IsochroneEdge]
    }
}

trait SomeIsochroneComputerComponent {
    self: IsochroneComputerComponent =>

    val isoComputer: IsochroneComputer
}
