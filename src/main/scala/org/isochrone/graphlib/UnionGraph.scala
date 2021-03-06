package org.isochrone.graphlib

class UnionGraph[NodeType](grp1: GraphType[NodeType], grp2: GraphType[NodeType]) extends GraphType[NodeType] {
    def neighbours(nd: NodeType) = grp1.neighbours(nd) ++ grp2.neighbours(nd)

    def nodes = grp1.nodes ++ grp2.nodes

    def edgeCost(start: NodeType, end: NodeType) = grp1.edgeCost(start, end).orElse(grp2.edgeCost(start, end))
}