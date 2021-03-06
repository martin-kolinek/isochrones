package org.isochrone.dijkstra

import org.isochrone.graphlib._
import org.isochrone.compute.IsochroneComputerComponent
import org.isochrone.compute.SomeIsochroneComputerComponent

trait MultiLevelDijkstraComponent extends IsochroneComputerComponent with SomeIsochroneComputerComponent {
    self: MultiLevelGraphComponent with DijkstraAlgorithmProviderComponent =>

    object MultilevelDijkstra extends IsochroneComputer {
        private case class FromUpperLevel(isRegionDone: RegionType => Boolean, continueFrom: Traversable[(NodeType, Double)])

        private def iso(start: Traversable[(NodeType, Double)], rest: List[GraphWithRegionsType[NodeType, RegionType]], lowerLevel: Option[GraphWithRegionsType[NodeType, RegionType]], limit: Double): FromUpperLevel = {
            if (rest.isEmpty) FromUpperLevel(x => false, start)
            else {
                val curLevel = rest.head
                val grouped = start.groupBy(x => curLevel.nodeRegion(x._1)).collect {
                    case (Some(reg), x) => reg -> x
                }.toSeq
                val singleResult = for {
                    (reg, regStart) <- grouped
                    single = curLevel.singleRegion(reg)
                    res <- dijkstraForGraph(single).nodesWithin(regStart, limit)
                } yield res
                val fromUpperLevel = iso(singleResult, rest.tail, Some(curLevel), limit)
                val dijkstraOnUndone = dijkstraForGraph(curLevel.filterRegions(x => !fromUpperLevel.isRegionDone(x)))
                val borderNodes = dijkstraOnUndone.nodesWithin(fromUpperLevel.continueFrom, limit).toList
                val doneRegions = (for {
                    (nd, cst) <- borderNodes
                    rem = limit - cst
                    rg <- lowerLevel.flatMap(_.nodeRegion(nd))
                    diam <- lowerLevel.map(_.regionDiameter(rg))
                    if diam <= rem
                } yield rg).toSet
                FromUpperLevel(doneRegions.contains, borderNodes)
            }
        }

        def isochrone(start: Traversable[(NodeType, Double)], limit: Double) = {
            val res = iso(start, levels.toList, None, limit)
            res.continueFrom.map {
                case (id, fromStart) => IsochroneNode(id, limit - fromStart)
            }
        }
    }

    val isoComputer = MultilevelDijkstra

}

