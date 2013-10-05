package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry

class EdgeTable(name: String) extends Table[(Long, Long, Double, Boolean)](name) {
    def start = column[Long]("start_node")
    def end = column[Long]("end_node")
    def cost = column[Double]("cost")
    def virtual = column[Boolean]("virtual")
    def * = start ~ end ~ cost ~ virtual
}

class NodeTable(name: String) extends Table[(Long, Int, Geometry)](name) {
    def id = column[Long]("id")
    def region = column[Int]("region")
    def geom = column[Geometry]("geom")
    def * = id ~ region ~ geom
}

class RegionTable(name: String) extends Table[(Int, Double)](name) {
    def id = column[Int]("id")
    def diameter = column[Double]("diameter")
    def * = id ~ diameter
}

trait RoadNetTables {
    val roadNet: EdgeTable
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
    val roadNetTables = new DefaultRoadNetTablesWithPrefix("")
}

trait MultiLevelRoadNetTableComponent {
    val roadNetTableLevels: Seq[RoadNetTables]
}

trait DefaultTwoLevelRoadNetTableComponent {
    val roadNetTableLevels = Seq(new DefaultRoadNetTablesWithPrefix(""), new DefaultRoadNetTablesWithPrefix("upper_"))
}

class DefaultRoadNetTablesWithPrefix(prefix: String) extends RoadNetTables {
    val roadNet = new EdgeTable(prefix + "road_net")
    val roadNodes = new NodeTable(prefix + "road_nodes")
    val roadRegions = new RegionTable(prefix + "road_regions")
}