package org.isochrone.simplegraph

import org.isochrone.graphlib._
import org.isochrone.dijkstra.DijkstraProvider

trait SimpleGraphComponent {
    self: DijkstraProvider =>

    type NodeType = Int
    type RegionType = SimpleGraphRegion

    case class SimpleGraphRegion private[SimpleGraphComponent] (num: Int, sg: SimpleGraph) extends Region {
        lazy val diameter = sg.nodes.filter(x => sg.nodeRegion(x) == Some(num)).map(sg.nodeEccentricity).max
    }

    class SimpleGraph private (edges: Seq[(Int, Int, Double)], nodeRegions: Map[Int, Int]) extends GraphWithRegionsType[Int, SimpleGraphRegion] {
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

        def neighbours(node: Int) = neigh.getOrElse(node, Seq())

        def nodeRegion(node: Int) = nodeRegionsProc.get(node)

        def nodeEccentricity(n: Int) = nodeEccentrities(n)

        override def toString = s"SimpleGraph($edges)"
    }

    object SimpleGraph {
        def apply(edges: (Int, Int, Double)*) = new SimpleGraph(edges, (edges.map(_._1) ++ edges.map(_._2)).map(_ -> 0).toMap)
        def apply(edges: Seq[(Int, Int, Double)], nodes: Map[Int, Int]) = new SimpleGraph(edges, nodes)
    }
}