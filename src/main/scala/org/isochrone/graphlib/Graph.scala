package org.isochrone.graphlib

trait GraphType[Node] {
    def neighbours(nd:Node):Traversable[(Node, Double)]
    def nodes:Traversable[Node]
}

trait GraphComponent {
    type NodeType
    
    val graph:GraphType[NodeType]
}
