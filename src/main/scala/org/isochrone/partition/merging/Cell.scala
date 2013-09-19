package org.isochrone.partition.merging

import org.isochrone.graphlib._
import scala.collection.immutable.Set

trait CellComponent {
	self:GraphComponent =>
    
    class Cell private (val nodes: Set[NodeType], val leaving: Set[(NodeType, NodeType)]) {
        def ++(other: Cell) =
            new Cell(nodes ++ other.nodes,
                leaving.filter(x => !other.nodes.contains(x._2)) ++ other.leaving.filter(x => !nodes.contains(x._2)))
        lazy val boundarySize = leaving.map(_._1).toSet.size
        lazy val size = nodes.size
        lazy val edges = nodes.flatMap(graph.neighbours _).filter(x => nodes.contains(x._1)).size

        override def toString = s"Cell($nodes)"
    }

    object Cell {
        def apply(nd: NodeType) = new Cell(Set(nd), graph.neighbours(nd).map(nd -> _._1).toSet)
    }
}