package org.isochrone.graphlib

trait NodePosition[NodeType] {
    def nodePosition(nd: NodeType): Option[(Double, Double)]
}

trait NodePositionComponent extends GraphComponent {
    val graph: NodePosition[NodeType] with GraphType[NodeType]
}