package org.isochrone.areas

import org.isochrone.graphlib.GraphComponentBase

trait AreaComponent {
    self: GraphComponentBase =>
        
    case class Area(nodes: List[NodeType]) {
        override def equals(other: Any) = {
            other match {
                case Area(onodes) => {
                    (onodes ++ onodes).containsSlice(nodes) && (nodes ++ nodes).containsSlice(onodes)
                }
                case _ => false
            }
        }

        override lazy val hashCode = nodes.toSet.hashCode
    }

}