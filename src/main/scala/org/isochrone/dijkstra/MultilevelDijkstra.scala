package org.isochrone.dijkstra

import org.isochrone.graphlib._

trait MultiLevelDijkstraComponent {
    self: MultiLevelGraphComponent =>
    
    object MultilevelDijkstra {//[T, Node, Region](levels: List[T])(implicit ev: HasRegions[T, Node, Region], ev2: IsGraph[T, Node]) {
        private def dijkstraComp(g:GraphType[NodeType]) = {
            new DijkstraAlgorithmComponent with GraphComponent {
                type NodeType = self.NodeType
                val graph = g
            }
        }
        
        private def iso(start: Traversable[(NodeType, Double)], rest: List[GraphWithRegionsType[NodeType, RegionType]], limit: Double): Traversable[(NodeType, Double)] = {
            if (rest.isEmpty) start
            else {
                val curLevel = rest.head
                val grouped = start.groupBy(x => curLevel.nodeRegion(x._1)).collect {
                    case (Some(reg), x) => reg -> x
                }.toSeq
                val singleResult = for {
                    (reg, regStart) <- grouped
                    single = curLevel.singleRegion(reg)
                    res <- dijkstraComp(single).DijkstraAlgorithm.isochrone(regStart, limit)
                } yield res
                val fromUpperLevel = iso(singleResult, rest.tail, limit)
                singleResult ++ dijkstraComp(curLevel).DijkstraAlgorithm.isochrone(fromUpperLevel.filter(x => limit - x._2 < curLevel.nodeEccentricity(x._1)), limit)
                fromUpperLevel
            }
        }

        def isochrone(start: NodeType, limit: Double) = {
            iso(Seq((start, 0.0)), levels.toList, limit).groupBy(_._1).map(x => x._1 -> x._2.map(_._2).min)
        }
    }

}

