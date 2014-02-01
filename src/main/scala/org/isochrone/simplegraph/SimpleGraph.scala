package org.isochrone.simplegraph

import org.isochrone.graphlib._
import shapeless.Lens._
import org.isochrone.dijkstra.DijkstraProvider

trait SimpleGraphComponent extends GraphComponentBase {
    class SimpleGraph private (neigh: Map[NodeType, Map[NodeType, Double]], nodePositions: Map[NodeType, (Double, Double)]) extends MapGraphType[NodeType] with NodePosition[NodeType] {
        def this(edges: Seq[(NodeType, NodeType, Double)], nodePositions: Map[NodeType, (Double, Double)]) =
            this(edges.groupBy(_._1).map { case (k, v) => (k, v.map(x => (x._2, x._3)).toMap) }, nodePositions)

        def nodes: Traversable[NodeType] = (neigh.keys ++ neigh.values.flatMap(identity).map(_._1)).toSet

        def neighbours(node: NodeType) = neigh.getOrElse(node, Map())

        def nodePosition(nd: NodeType) = nodePositions(nd)

        override def toString = s"SimpleGraph($neigh)"

        def withEdge(start: NodeType, end: NodeType, cost: Double) = {
            val lns = mapLens[NodeType, Map[NodeType, Double]](start)
            val newNeigh = lns.modify(neigh) {
                case None => Some(Map(end -> cost))
                case Some(l) => Some(l + (end -> cost))
            }
            new SimpleGraph(newNeigh, nodePositions)
        }

        def withoutEdge(start: NodeType, end: NodeType) = {
            val lns = mapLens[NodeType, Map[NodeType, Double]](start)
            val newNeigh = lns.modify(neigh) {
                case None => None
                case Some(l) => Some(l - end)
            }
            new SimpleGraph(newNeigh, nodePositions)
        }

        def extractEdges = neigh.flatMap {
            case (start, n) => n.map {
                case (end, cost) => (start, end, cost)
            }
        }

        def contains(start: NodeType, end: NodeType) = {
            neighbours(start).exists(_._1 == end)
        }
    }

    object SimpleGraph {
        def apply(edges: (NodeType, NodeType, Double)*) = new SimpleGraph(edges, (edges.map(_._1) ++ edges.map(_._2)).map(_ -> (0.0 -> 0.0)).toMap)

        def undirOneCost(edges: (NodeType, NodeType)*) = {
            undirCost(1)(edges: _*)
        }

        def undirCost(cst: Double)(edges: (NodeType, NodeType)*) = {
            val dir = (edges ++ edges.map(_.swap)).map(x => (x._1, x._2, cst))
            apply(dir: _*)
        }
    }
}

trait SimpleGraphWithRegionsComponent extends SimpleGraphComponent {
    self: DijkstraProvider =>

    type RegionType = SimpleGraphRegion

    case class SimpleGraphRegion private[SimpleGraphWithRegionsComponent] (num: Int, sg: SimpleGraphWithRegions) {
        lazy val diameter = {
            val eccs = sg.nodes.filter(x => sg.nodeRegion(x) == Some(this)).map(sg.nodeEccentricity)
            if (eccs.isEmpty)
                Double.PositiveInfinity
            else
                eccs.max
        }

        override def toString = s"SimpleRegion($num, diam=$diameter)"
    }

    class SimpleGraphWithRegions(edges: Seq[(NodeType, NodeType, Double)], nodeRegions: Map[NodeType, Int], nodePositions: Map[NodeType, (Double, Double)]) extends SimpleGraph(edges, nodePositions) with GraphWithRegionsType[NodeType, RegionType] {
        val nodeRegionsProc = nodeRegions.mapValues(SimpleGraphRegion(_, this))

        lazy val nodeEccentrities = {
            val x = for {
                node <- nodes.toSeq
                region <- nodeRegions.get(node)
                single = this.singleRegion(nodeRegionsProc(node))
            } yield node -> dijkstraForGraph(single).DijkstraAlgorithm.compute(Seq(node -> 0.0)).map(_._2).max
            x.toMap
        }

        def regionDiameter(rg: SimpleGraphRegion) = rg.diameter

        def regions: Traversable[SimpleGraphRegion] = nodeRegionsProc.map(_._2).toSet

        def nodeRegion(node: NodeType) = nodeRegionsProc.get(node)

        def nodeEccentricity(n: NodeType) = nodeEccentrities(n)
    }

    object SimpleGraphWithRegions {
        def apply(edges: (NodeType, NodeType, Double)*) = new SimpleGraphWithRegions(edges, (edges.map(_._1) ++ edges.map(_._2)).map(x => x -> 1).toMap, (edges.map(_._1) ++ edges.map(_._2)).map(_ -> (0.0 -> 0.0)).toMap)
        def apply(edges: Seq[(NodeType, NodeType, Double)], nodes: Map[NodeType, Int]) = new SimpleGraphWithRegions(edges, nodes, nodes.map(_._1 -> (0.0 -> 0.0)).toMap)
        def apply(edges: Seq[(NodeType, NodeType, Double)], regions: Map[NodeType, Int], positions: Map[NodeType, (Double, Double)]) = new SimpleGraphWithRegions(edges, regions, positions)
    }
}
