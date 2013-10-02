package org.isochrone.util

import scala.util.Random
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DijkstraProvider

trait RandomGraphComponent extends SimpleGraphComponent {
    self: DijkstraProvider =>
    type NodeType = Int
    object RandomGraph {
        val rand = new Random()

        def directed(n: Int, e: Int) = {
            val rands = Iterator.continually(rand.nextInt(n)).grouped(2).
                filter { case Seq(a, b) => a != b }.map(_.toSet).take(e).toSet

            rands.map(_.toSeq).map { case Seq(a, b) => (a, b) }
        }

        def randomSymmetricGraph(n: Int, e: Int) = {
            val dir = directed(n, e)
            val undirected = dir ++ dir.map(_.swap)
            val weighted = undirected.map(x => (x._1, x._2, 1.0))
            SimpleGraph(weighted.toSeq: _*)
        }

        def randomGraph(n: Int, e: Int) = {
            val dir = directed(n, e)
            val weighted = dir.map(x => (x._1, x._2, rand.nextDouble + 1.0))
            SimpleGraph(weighted.toSeq: _*)
        }
    }
}