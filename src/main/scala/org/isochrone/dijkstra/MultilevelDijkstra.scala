package org.isochrone.dijkstra

import org.isochrone.graphlib._
import org.isochrone.compute.IsochroneComputerComponent

trait MultiLevelDijkstraComponent extends IsochroneComputerComponent {
    self: MultiLevelGraphComponent =>

    object MultilevelDijkstra extends IsochroneComputer {
        private def dijkstraComp(g: GraphType[NodeType]) = {
            new DijkstraAlgorithmComponent with GraphComponent {
                type NodeType = self.NodeType
                val graph = g
            }
        }

        private case class FromUpperLevel(isRegionDone: RegionType => Boolean, continueFrom: Traversable[(NodeType, Double)])

        private def iso(start: Traversable[(NodeType, Double)], rest: List[GraphWithRegionsType[NodeType, RegionType]], lowerNodeRegion: NodeType => Option[RegionType], limit: Double): FromUpperLevel = {
            if (rest.isEmpty) FromUpperLevel(x => false, start)
            else {
                val curLevel = rest.head
                val grouped = start.groupBy(x => curLevel.nodeRegion(x._1)).collect {
                    case (Some(reg), x) => reg -> x
                }.toSeq
                val singleResult = for {
                    (reg, regStart) <- grouped
                    single = curLevel.singleRegion(reg)
                    res <- dijkstraComp(single).DijkstraAlgorithm.nodesWithin(regStart, limit)
                } yield res
                val fromUpperLevel = iso(singleResult, rest.tail, curLevel.nodeRegion, limit)
                val dijkstraOnUndone = dijkstraComp(curLevel.filterRegions(fromUpperLevel.isRegionDone))
                val borderNodes = dijkstraOnUndone.DijkstraAlgorithm.nodesWithin(fromUpperLevel.continueFrom, limit)
                val doneRegions = (for {
                    (nd, rem) <- borderNodes
                    rg <- curLevel.nodeRegion(nd)
                    if rg.diameter <= rem
                } yield rg).toSet
                FromUpperLevel(doneRegions.contains, borderNodes)
            }
        }

        def isochrone(start: Traversable[(NodeType, Double)], limit: Double) = {
            val res = iso(start, levels.toList, nd => None, limit)
            val nodeSet = res.continueFrom.map(_._1).toSet
            for {
                (nd, cost) <- res.continueFrom
                remaining = limit - cost
                (neigh, ncost) <- levels.head.neighbours(nd)
                if !nodeSet.contains(neigh) && levels.head.nodeRegion(neigh).filter(res.isRegionDone).isEmpty
                quotient = remaining / ncost
                if quotient <= 1
            } yield IsochroneEdge(nd, neigh, quotient)
        }
    }

}

