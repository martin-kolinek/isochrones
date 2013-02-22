package org.isochrone.dbgraph

import org.isochrone.util.LRUCache
import scala.collection.mutable.HashMap
import scala.slick.driver.BasicDriver.simple._

class DatabaseGraph(tables:GraphTables, maxRegions:Int, retrieveNotification: =>Unit)(implicit session:Session) {
    def this(tables:GraphTables, maxRegions:Int)(implicit session:Session) = this(tables, maxRegions, {})
	type Region = Int
	type Node = Long
	private val regions = new LRUCache[Region, Traversable[Node]]((k, v, m) => {
		val ret = m.size > maxRegions
		if(ret)
			removeRegion(v)
		ret
	})
	
	private val neighbours = new HashMap[Node, Traversable[(Node, Double)]]
	
	private val nodesToRegions = new HashMap[Node, Region]
	
	def removeRegion(nodes:Traversable[Node]) = for(n<-nodes) {
		nodesToRegions -= n
		neighbours -= n
	} 

    def nodeRegion(node:Node) = {
        ensureRegion(node)
        nodesToRegions.get(node)
    }
	
	def nodesInMemory = neighbours.size

    def ensureRegion(node:Node) {
        if(!nodesToRegions.isDefinedAt(node))
            retrieveNode(node)
    }

    def ensureInMemory(node:Node) {
        if(!neighbours.isDefinedAt(node)) { 
			retrieveNode(node)
		}
    }
	
	def getNeighbours(node:Node) = {
		ensureInMemory(node)
		if(nodesToRegions.contains(node))
			regions.updateUsage(nodesToRegions(node))
		neighbours.getOrElse(node, Seq())
	}
	
	def retrieveNode(node:Node) {
        retrieveNotification
		val q = tables.nodes.filter(_.id === node).map(_.region)
		q.list().map(retrieveRegion)
	}
	
	def retrieveRegion(region:Region) {
		val startJoin = tables.nodes leftJoin tables.edges on((n, e)=>n.id===e.start)
		val q = for((n, e) <- startJoin.sortBy(_._1.id) if n.region === region) yield n.id ~ e.end.? ~ e.cost.?
		val list = q.list()
		regions(region) = list.map(_._1)
		val map = list.groupBy(_._1)
		for((k, v) <- map) {
			neighbours(k)=v.collect{case (st, Some(en), Some(c)) => (en, c)}
			nodesToRegions(k) = region
		}
	}
	
	def graphlib = new HasNeighboursInstance(this).nodeHasNeighbours
	
	def allNodes = tables.nodes.map(_.id).list
}

