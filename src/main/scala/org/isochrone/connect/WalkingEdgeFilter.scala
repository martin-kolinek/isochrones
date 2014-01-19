package org.isochrone.connect

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dbgraph.DatabaseGraph
import org.isochrone.graphlib.UnionGraph
import org.isochrone.dijkstra.DijkstraProvider
import scala.annotation.tailrec
import com.typesafe.scalalogging.slf4j.Logging

trait WalkingEdgeFilter extends GraphComponentBase with Logging {
    self: SimpleGraphComponent with DijkstraProvider =>

    type NodeType = Long

    @tailrec
    private def filterNodesInternal(edges: List[(NodeType, NodeType, Double)], dbg: DatabaseGraph, sg: SimpleGraph, filtered: List[(NodeType, NodeType, Double)]): List[(NodeType, NodeType, Double)] = edges match {
        case Nil => filtered
        case (edg@(start, end, cost)) :: rest => {
            logger.debug(s"Processing ${(start, end)}")
            val withoutEdge = sg.withoutEdge(start, end).withoutEdge(end, start)
            val union = new UnionGraph(dbg, withoutEdge)
            val dijk = dijkstraForGraph(union)
            if (dijk.DijkstraHelpers.distance(start, end) <= cost && dijk.DijkstraHelpers.distance(end, start) <= cost)
                filterNodesInternal(rest, dbg, withoutEdge, filtered)
            else
                filterNodesInternal(rest, dbg, sg, edg :: filtered)
        }
    }

    def filterNodes(sg: SimpleGraph, dbg: DatabaseGraph) = {
        val edgList = sg.extractEdges.groupBy {
            case (st, en, cst) => Set(st, en)
        }.map {
            case (_, l) => l.head
        }.toList
        logger.info(s"Filtering simple graph nodes, size before = ${edgList.size}")
        val filtered = filterNodesInternal(edgList, dbg, sg, Nil)
        logger.info(s"Done filtering, new size = ${filtered.size}")
        val bothWays = filtered ++ filtered.map {
            case (s, e, c) => (e, s, c)
        }
        bothWays.filter {
            case (s, e, c) => sg.contains(s, e)
        }
    }
}