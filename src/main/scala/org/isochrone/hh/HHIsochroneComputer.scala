package org.isochrone.hh

import org.isochrone.compute.IsochroneComputerComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.db.MultiLevelRoadNetTableComponent
import org.isochrone.dbgraph.MultiLevelHHDatabaseGraphComponent
import scala.collection.mutable.ListBuffer
import org.isochrone.dijkstra.GenericDijkstraAlgorithmProvider
import org.isochrone.compute.SomeIsochroneComputerComponent
import scala.collection.mutable.HashSet

trait HHIsochroneComputer extends SomeIsochroneComputerComponent with QueryGraphComponent with GraphComponentBase {
    self: GenericDijkstraAlgorithmProvider with MultiLevelHHDatabaseGraphComponent =>
    type NodeType = Long
    object HHIsoComputer extends IsochroneComputer {
        def isochrone(start: Traversable[(NodeType, Double)], max: Double) = {
            def withLevelZero(n: (NodeType, Double)) = (NodeWithLevel(n._1, 0), n._2)
            val qg = new QueryGraph(hhDbGraphs.toIndexedSeq, shortcutGraphs, reverseShortcutGraph, max)
            val dijk = dijkstraForGraph(qg)
            val result = new ListBuffer[IsochroneNode]
            var stop = false
            dijk.alg(start.map(withLevelZero), (cl, clc, prev) => {
                logger.debug(s"Closing $cl with cost $clc from $prev")
                qg.onClosed(cl, clc, prev)
                if (clc <= max)
                    result += IsochroneNode(cl.nd, max - clc)
                else
                    stop = true
            }, qg.onOpened, () => stop)
            result.distinct.toList
        }
    }

    val isoComputer = HHIsoComputer
}