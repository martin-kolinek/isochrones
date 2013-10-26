package org.isochrone.graphlib

trait NodePosition[NodeType] {
    def nodePosition(nd: NodeType): (Double, Double)
}

trait NodePositionComponent extends GraphComponentBase {
    val nodePos: NodePosition[NodeType]
}