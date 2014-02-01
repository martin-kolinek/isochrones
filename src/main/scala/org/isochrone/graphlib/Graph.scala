package org.isochrone.graphlib

trait GraphType[Node] {
    def neighbours(nd: Node): Traversable[(Node, Double)]
    def nodes: Traversable[Node]
    def edgeCost(start: Node, end: Node): Option[Double]
}

trait MapGraphType[Node] extends GraphType[Node] {
    def neighbours(nd: Node): Map[Node, Double]
    def edgeCost(start: Node, end: Node) = neighbours(start).get(end)
}

trait GraphComponent extends GraphComponentBase {

    val graph: GraphType[NodeType]
}

trait GraphComponentBaseWithDefault extends GraphComponentBase {
    def noNode: NodeType
}

trait GraphComponentBase {
    type NodeType
}