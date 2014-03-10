package org.isochrone.hh

import org.isochrone.graphlib.GraphComponentBase

trait HHProps[NodeType] extends DescendLimitProvider[NodeType] with ShortcutReverseLimitProvider[NodeType] with NeighbourhoodSizes[NodeType] {
    def hasHigherLevel(nd: NodeType): Boolean
}

trait HHPropsComponent {
    self: GraphComponentBase =>
    val hhProps: HHProps[NodeType]
}