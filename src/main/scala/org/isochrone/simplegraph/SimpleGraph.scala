package org.isochrone.simplegraph

import org.isochrone.graphlib._
import org.isochrone.dijkstra.DijkstraProvider

trait SimpleGraphComponent extends GraphComponentBase {
    self: DijkstraProvider =>

    type RegionType = SimpleGraphRegion

    case class SimpleGraphRegion private[SimpleGraphComponent] (num: Int, sg: SimpleGraph) {
        lazy val diameter = {
            val eccs = sg.nodes.filter(x => sg.nodeRegion(x) == Some(this)).map(sg.nodeEccentricity)
            if (eccs.isEmpty)
                Double.PositiveInfinity
            else
                eccs.max
        }

        override def toString = s"SimpleRegion($num, diam=$diameter)"
    }

    class SimpleGraph(edges: Seq[(NodeType, NodeType, Double)], nodeRegions: Map[NodeType, Int]) extends GraphWithRegionsType[NodeType, SimpleGraphRegion] {
        private val neigh = edges.groupBy(_._1).map { case (k, v) => (k, v.map(x => (x._2, x._3))) }

        val nodeRegionsProc = nodeRegions.mapValues(new SimpleGraphRegion(_, this))

        lazy val nodeEccentrities = {
            val x = for {
                node <- nodes.toSeq
                region <- nodeRegions.get(node)
                single = this.singleRegion(nodeRegionsProc(node))
            } yield node -> dijkstraForGraph(single).DijkstraAlgorithm.compute(Seq(node -> 0.0)).map(_._2).max
            x.toMap
        }

        def nodes = (neigh.keys ++ neigh.values.flatMap(identity).map(_._1)).toSet

        def neighbours(node: NodeType) = neigh.getOrElse(node, Seq())

        def nodeRegion(node: NodeType) = nodeRegionsProc.get(node)

        def nodeEccentricity(n: NodeType) = nodeEccentrities(n)

        def regionDiameter(rg: RegionType) = rg.diameter

        override def toString = s"SimpleGraph($edges)"
    }

    object SimpleGraph {
        def apply(edges: (NodeType, NodeType, Double)*) = new SimpleGraph(edges, (edges.map(_._1) ++ edges.map(_._2)).map(_ -> 0).toMap)
        def apply(edges: Seq[(NodeType, NodeType, Double)], nodes: Map[NodeType, Int]) = new SimpleGraph(edges, nodes)
    }
}