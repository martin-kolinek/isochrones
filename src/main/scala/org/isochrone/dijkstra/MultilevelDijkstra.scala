package org.isochrone.dijkstra

import org.isochrone.graphlib._

class MultilevelDijstra[T, Node, Region](levels:List[T])(implicit ev:IsGraphWithRegions[T, Node, Region]) {
	private def isochrone(start:Traversable[(Node, Double)], rest:List[T], limit:Double):Traversable[(Node, Double)] = {
		if(rest.isEmpty) start
		else {
			val curLevel = rest.head
					
			val grouped = start.groupBy(x=>curLevel.nodeRegion(x._1))
			assert(grouped.map(_._1).map(_.isEmpty).forall(!_))
			val singleResult = for {
				(reg, regStart) <- grouped
				single = new SingleRegionGraph(curLevel, reg.get)
				res <- DijkstraAlgorithm.isochrone(regStart, limit)(single.instance)
			} yield res
			val fromUpperLevel = isochrone(singleResult, rest.tail, limit)
		    DijkstraAlgorithm.isochrone(fromUpperLevel.filter(x=> limit - x._2 < curLevel.eccentricity(x._1)), limit)
		}
	}
}
