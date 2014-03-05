package org.isochrone.hh

import org.isochrone.graphlib.GraphComponentBase

trait DescendLimitProvider[NodeType] {
    def descendLimit(nd: NodeType): Double
}

trait DescendLimitComponent {
    self: GraphComponentBase =>
    val descendLimitProvider: NeighbourhoodSizes[NodeType]
}

trait ShortcutReverseLimitProvider[NodeType] {
    def shortcutReverseLimit(nd: NodeType): Double
}

trait ShortcutLimitComponent {
    self: GraphComponentBase =>
    val shortcutRevLimitProvider: ShortcutReverseLimitProvider[NodeType]
}
