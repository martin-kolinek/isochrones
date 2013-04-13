package org.isochrone.dijkstra

import org.isochrone.graphlib._

class MultilevelDijkstra[T, Node, Region](levels:List[T])(implicit ev:HasRegions[T, Node, Region], ev2:IsGraph[T, Node]) {
	private def iso(start:Traversable[(Node, Double)], rest:List[T], limit:Double):Traversable[(Node, Double)] = {
		if(rest.isEmpty) start
		else {
			val curLevel = rest.head
					
			val grouped = start.groupBy(x=>curLevel.nodeRegion(x._1)).collect{
				case (Some(reg), x) => reg ->x
			}.toSeq
			val singleResult = for {
				(reg, regStart) <- grouped
				single = new SingleRegionGraph(curLevel, reg)
				res <- DijkstraAlgorithm.isochrone(regStart, limit)(single.instance)
			} yield res
			val fromUpperLevel = iso(singleResult, rest.tail, limit)
		    singleResult ++ DijkstraAlgorithm.isochrone(fromUpperLevel.filter(x=> limit - x._2 < curLevel.eccentricity(x._1)), limit)(curLevel.instance)
		}
	}
	
	def isochrone(start:Node, limit:Double) = {
		iso(Seq((start, 0.0)), levels, limit).groupBy(_._1).map(x=>x._1 -> x._2.map(_._2).min)
	}
}
