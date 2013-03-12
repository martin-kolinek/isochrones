package org.isochrone.dbgraph

import scala.slick.driver.BasicDriver._

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

class GraphTables(nodeName:String, edgeName:String) {
	val nodes = new NodeTable(nodeName)
	val edges = new EdgeTable(edgeName)
}