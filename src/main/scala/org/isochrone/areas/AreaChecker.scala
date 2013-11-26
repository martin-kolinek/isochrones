package org.isochrone.areas

import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphComponentBase

trait AreaCheckerComponent extends AreaComponent {
    self: GraphComponent =>

    object AreaChecker {
        def areaHasDiagonals(ar: Area) = {
            val nodeSet = ar.nodes.toSet
            (ar.nodes ++ ar.nodes.take(2)).sliding(3).exists {
                case Seq(a, b, c) =>
                    graph.neighbours(b).map(_._1).filterNot(_ == a).filterNot(_ == c).exists(nodeSet.contains)
            }
        }
    }
}