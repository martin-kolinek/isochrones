package org.isochrone.dijkstra

import org.isochrone.graphlib._
import org.isochrone.dbgraph.DatabaseGraph
import org.isochrone.dbgraph.SingleRegionDBGraph

class MultilevelDijkstra[T](levels:List[T])(implicit val imp:IsGraphWithRegions[T]) {

    type Node = imp.Node
    type Region = imp.Region

    def isochrone(start:Node, limit:Double) = {
        
    }

    private def isochrone(start:Traversable[(Node, Double)], rest:List[T], limit:Double):Traversable[(Node, Double)] = {
        if(rest.isEmpty) start
        else {
            val curLevel = rest.head
            
            val grouped = start.groupBy(x=>imp.nodeRegion(curLevel, x._1))
            //assert(grouped.map(_._1).map(_.isEmpty).forall(!_))
            /*val singleResult = for {
                (reg, regStart) <- grouped
                single = new SingleRegionDBGraph(curLevel, reg.get)
                res <- DijkstraAlgorithm.isochrone(regStart, limit)(single.graphlib)
            } yield res
            val fromUpperLevel = isochrone(singleResult, rest.tail, limit)
            fromUpperLevel*/
        }
        ???
    }
}
