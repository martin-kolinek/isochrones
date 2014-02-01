package org.isochrone.areas

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.GraphType
import shapeless.Lens
import org.isochrone.graphlib.MapGraphType

trait AreaGraphComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    class AreaWithDiagonalsGraph(mp: Map[NodeType, Map[NodeType, Double]]) extends MapGraphType[NodeType] {
        def withoutEdge(n1: NodeType, n2: NodeType) = {
            rem(n1, n2).rem(n2, n1)
        }

        private def rem(n1: NodeType, n2: NodeType) = {
            val lns = Lens.mapLens[NodeType, Map[NodeType, Double]](n1)
            new AreaWithDiagonalsGraph(lns.modify(mp)(opt => for {
                mp <- opt
            } yield mp - n2))
        }

        def nodes = mp.keys

        def neighbours(nd: NodeType) = mp(nd)

    }

    object AreaWithDiagonalsGraph {
        def apply(ar: PosArea, diagonals: Traversable[EdgeWithCost]) = {
            val fromAr = (ar.points :+ ar.points.head).sliding(2).flatMap {
                case Seq(a, b) => {
                    Seq((a.nd, b.nd, ar.cost(a.nd, b.nd)), (b.nd, a.nd, ar.cost(b.nd, a.nd)))
                }
            }

            val fromDiag = diagonals.flatMap { edg =>
                val Seq(a, b) = edg.nds.toSeq
                Seq((a, b, edg.cost), (b, a, edg.cost))
            }

            val mp = (fromAr ++ fromDiag).toSeq.view.groupBy(_._1).map {
                case (k, lst) => k -> lst.view.map(x => (x._2, x._3)).toMap
            }

            new AreaWithDiagonalsGraph(mp)
        }
    }

}