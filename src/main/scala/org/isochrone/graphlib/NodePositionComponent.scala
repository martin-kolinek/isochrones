package org.isochrone.graphlib

trait NodePosition[NodeType] {
    def nodePosition(nd: NodeType): Option[(Double, Double)]
}

trait NodePositionComponent extends GraphComponentBase {
    val graph: NodePosition[NodeType]
}