package dbgraph

import util.LRUCache
import scala.collection.mutable.HashMap
import scala.slick.driver.BasicDriver.simple._
import scala.collection.mutable.HashSet

class DatabaseGraph(tables:GraphTables, maxRegions:Int)(implicit session:Session) {
	type Region = Int
	type Node = Long
	val regions = new LRUCache[Region, Traversable[Node]]((k, v, m) => {
		val ret = m.size > maxRegions
		if(ret)
			removeRegion(v)
		ret
	})
	
	val neighbours = new HashMap[Node, Traversable[(Node, Double)]]
	
	val nodesToRegions = new HashMap[Node, Region]
	
	def removeRegion(nodes:Traversable[Node]) = for(n<-nodes) {
		nodesToRegions -= n
		neighbours -= n
	} 
	
	def getNeighbours(node:Node) = {
		if(!neighbours.isDefinedAt(node)) { 
			retrieveNode(node)
		}
		regions.updateUsage(nodesToRegions(node))
		neighbours(node)
	}
	
	def retrieveNode(node:Node) {
		val q = tables.nodes.filter(_.id === node).map(_.region)
		q.list().map(retrieveRegion)
	}
	
	def retrieveRegion(region:Region) {
		val startJoin = tables.nodes innerJoin tables.edges on((n, e)=>n.id===e.start)
		val q = for((n, e) <- startJoin.sortBy(_._2.start) if n.region === region) yield e.start ~ e.end ~ e.cost
		val list = q.list()
		regions(region) = list.map(_._1)
		val map = list.groupBy(_._1)
		for((k, v) <- map) {
			neighbours(k)=v.map{case (st, en, c) => (en, c)}
			nodesToRegions(k) = region
		}
	}
	
	def graphlib = new HasNeighboursInstance(this)
}

