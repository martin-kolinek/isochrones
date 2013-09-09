package org.isochrone.dbgraph

import org.isochrone.util.db.MyPostgresDriver.simple._

class EdgeTable(name:String) extends Table[(Long, Long, Double)](name) {
	def start = column[Long]("start_node")
	def end = column[Long]("end_node")
	def cost = column[Double]("cost")
	def * = start ~ end ~ cost
}

class NodeTable(name:String) extends Table[(Long, Int)](name) {
	def id = column[Long]("id")
	def region = column[Int]("region")
	def * = id ~ region
}

class RegionTable(name:String) extends Table[(Int, Double)](name) {
    def id = column[Int]("id")
    def diameter = column[Double]("diameter")
    def * = id ~ diameter
}

class GraphTables(nodeName:String, edgeName:String) {
	val nodes = new NodeTable(nodeName)
	val edges = new EdgeTable(edgeName)
}