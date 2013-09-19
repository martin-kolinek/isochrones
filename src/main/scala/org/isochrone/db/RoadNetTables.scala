package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._

class EdgeTable(name: String) extends Table[(Long, Long, Double)](name) {
    def start = column[Long]("start_node")
    def end = column[Long]("end_node")
    def cost = column[Double]("cost")
    def * = start ~ end ~ cost
}

class NodeTable(name: String) extends Table[(Long, Int)](name) {
    def id = column[Long]("id")
    def region = column[Int]("region")
    def * = id ~ region
}

class RegionTable(name: String) extends Table[(Int, Double)](name) {
    def id = column[Int]("id")
    def diameter = column[Double]("diameter")
    def * = id ~ diameter
}

trait RoadNetTables {
    val roadNet: EdgeTable
    val roadNetUndir: EdgeTable
    val roadNodes: NodeTable
    val roadRegions: RegionTable
}

trait RoadNetTableComponent {
    val roadNetTables: RoadNetTables
}

trait HigherLevelRoadNetTableComponent {
    val higherRoadNetTables: RoadNetTables
}

trait DefaultRoadNetTableComponent extends RoadNetTableComponent {
    val roadNetTables = new RoadNetTables {
        val roadNet = new EdgeTable("road_net")
        val roadNetUndir = new EdgeTable("road_net_undir")
        val roadNodes = new NodeTable("road_nodes")
        val roadRegions = new RegionTable("road_regions")
    }
}

trait MultiLevelRoadNetTableComponent {
    val roadNetTableLevels: Seq[RoadNetTables]
}

trait DefaultTwoLevelRoadNetTableComponent {
    val roadNetTableLevels = Seq(new RoadNetTables {
        val roadNet = new EdgeTable("road_net")
        val roadNetUndir = new EdgeTable("road_net_undir")
        val roadNodes = new NodeTable("road_nodes")
        val roadRegions = new RegionTable("road_regions") 
    }, new RoadNetTables {
        val roadNet = new EdgeTable("road_net_upper")
        val roadNetUndir = new EdgeTable("road_net_undir_upper")
        val roadNodes = new NodeTable("road_nodes_upper")
        val roadRegions = new RegionTable("road_regions_upper") 
    })
}