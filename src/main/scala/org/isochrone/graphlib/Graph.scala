package org.isochrone.graphlib

trait GraphType[Node] {
    def neighbours(nd:Node):Traversable[(Node, Double)]
    def nodes:Traversable[Node]
}

trait GraphComponent extends GraphComponentBase {
    val graph:GraphType[NodeType]
}

trait GraphComponentBase {
    type NodeType
}