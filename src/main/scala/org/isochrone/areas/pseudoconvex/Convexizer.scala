package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import shapeless.Lens
import org.isochrone.graphlib.GraphType
import org.isochrone.dijkstra.DijkstraProvider
import scala.annotation.tailrec
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.areas.PosAreaComponent

trait ConvexizerComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    trait Convexizer {
        def convexize(ar: Area, diagonals: Traversable[EdgeWithCost]): Traversable[EdgeWithCost]
    }

    val convexizer: Convexizer
}

trait HertelMehlhortModConvexizerComponent extends ConvexizerComponent {
    self: GraphComponentBase with DijkstraProvider with AllCostsForAreaComponent =>

    class AreaWithDiagonalsGraph(mp: Map[NodeType, Seq[(NodeType, Double)]]) extends GraphType[NodeType] {
        def withoutEdge(n1: NodeType, n2: NodeType) = {
            rem(n1, n2).rem(n2, n1)
        }

        private def rem(n1: NodeType, n2: NodeType) = {
            val lns = Lens.mapLens[NodeType, Seq[(NodeType, Double)]](n1)
            new AreaWithDiagonalsGraph(lns.modify(mp)(opt => for {
                lst <- opt
            } yield lst.filterNot(_._1 == n2)))
        }

        def nodes = mp.keys

        def neighbours(nd: NodeType) = mp(nd)

    }

    object AreaWithDiagonalsGraph {
        def apply(ar: Area, diagonals: Traversable[EdgeWithCost]) = {
            val fromAr = (ar.points :+ ar.points.head).sliding(2).flatMap {
                case Seq(a, b) => {
                    Seq((a.nd, b.nd, ar.cost(a.nd, b.nd)), (b.nd, a.nd, ar.cost(b.nd, a.nd)))
                }
            }

            val fromDiag = diagonals.flatMap { edg =>
                val Seq(a, b) = edg.nds.toSeq
                Seq((a, b, edg.cost), (b, a, edg.cost))
            }

            val mp = (fromAr ++ fromDiag).toSeq.groupBy(_._1).map {
                case (k, lst) => k -> lst.map(x => (x._2, x._3))
            }

            new AreaWithDiagonalsGraph(mp)
        }
    }

    object HertelMehlhortModConvexizer extends Convexizer with Logging {
        @tailrec
        private def conv(ar: AreaWithDiagonalsGraph, allCosts: List[EdgeWithCost], diags: List[EdgeWithCost], needed: List[EdgeWithCost]): List[EdgeWithCost] = diags match {
            case Nil => needed
            case candidate :: rest => {
                logger.debug(s"processing edge $candidate, remaining ${rest.size}")
                val Seq(a, b) = candidate.nds.toSeq
                val noedg = ar.withoutEdge(a, b)
                val comp = dijkstraForGraph(noedg)
                val notRequired = allCosts.flatMap { e =>
                    val Seq(a, b) = e.nds.toSeq
                    Seq((a, b, e.cost), (b, a, e.cost))
                }.forall {
                    case (a, b, cost) => {
                        val noedgCost = comp.DijkstraHelpers.distance(a, b)
                        noedgCost <= cost
                    }
                }
                if (notRequired)
                    conv(noedg, allCosts, rest, needed)
                else
                    conv(ar, allCosts, rest, candidate :: needed)
            }
        }

        def convexize(ar: Area, diagonals: Traversable[EdgeWithCost]) = {
            val grp = AreaWithDiagonalsGraph(ar, diagonals)
            conv(grp, allCostsForArea(ar).toList, diagonals.toList, Nil)
        }
    }

    val convexizer = HertelMehlhortModConvexizer
}