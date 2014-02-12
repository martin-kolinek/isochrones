package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import shapeless.Lens
import org.isochrone.graphlib.GraphType
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import scala.annotation.tailrec
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.areas.PosAreaComponent
import org.isochrone.areas.AreaGraphComponent

trait ConvexizerComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    trait Convexizer {
        def convexize(ar: PosArea, diagonals: Traversable[EdgeWithCost]): Traversable[EdgeWithCost]
    }

    val convexizer: Convexizer
}

trait HertelMehlhortModConvexizerComponent extends ConvexizerComponent with AreaGraphComponent {
    self: GraphComponentBase with DijkstraAlgorithmProviderComponent with AllCostsForAreaComponent =>

    object HertelMehlhortModConvexizer extends Convexizer with Logging {
        @tailrec
        private def conv(ar: AreaWithDiagonalsGraph, diags: List[EdgeWithCost], needed: List[EdgeWithCost]): List[EdgeWithCost] = diags match {
            case Nil => needed
            case candidate :: rest => {
                logger.debug(s"processing edge $candidate, remaining ${rest.size}")
                val Seq(a, b) = candidate.nds.toSeq
                val noedg = ar.withoutEdge(a, b)
                val comp = dijkstraForGraph(noedg)
                if (comp.helper.nodesWithin(a, candidate.cost).map(_._1).exists(b == _))
                    conv(noedg, rest, needed)
                else
                    conv(ar, rest, candidate :: needed)
            }
        }

        def convexize(ar: PosArea, diagonals: Traversable[EdgeWithCost]) = {
            val grp = AreaWithDiagonalsGraph(ar, diagonals)
            conv(grp, diagonals.toList, Nil)
        }
    }

    val convexizer = HertelMehlhortModConvexizer
}