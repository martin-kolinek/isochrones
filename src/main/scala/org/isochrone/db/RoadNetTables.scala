package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser
import shapeless.Lens
import scopt.OptionParser

class EdgeTable(name: String) extends Table[(Long, Long, Double, Boolean, Geometry)](name) {
    def start = column[Long]("start_node")
    def end = column[Long]("end_node")
    def cost = column[Double]("cost")
    def virtual = column[Boolean]("virtual")
    def geom = column[Geometry]("geom")
    def * = start ~ end ~ cost ~ virtual ~ geom
}

class NodeTable(name: String) extends Table[(Long, Int, Geometry)](name) {
    def id = column[Long]("id", O.PrimaryKey)
    def region = column[Int]("region")
    def geom = column[Geometry]("geom")
    def * = id ~ region ~ geom
}

class RegionTable(name: String) extends Table[(Int, Double)](name) {
    def id = column[Int]("id")
    def diameter = column[Double]("diameter")
    def * = id ~ diameter
}

class AreaTable(name: String) extends Table[(Long, Long, Int, Double)](name) {
    def id = column[Long]("id")
    def nodeId = column[Long]("node_id")
    def sequenceNo = column[Int]("sequence_no")
    def costToCover = column[Double]("cost_to_cover")
    def * = id ~ nodeId ~ sequenceNo ~ costToCover
}

class AreaGeometriesTable(name: String) extends Table[(Long, Geometry)](name) {
    def id = column[Long]("id")
    def geom = column[Geometry]("geom")
    def * = id ~ geom
}

trait RoadNetTables {
    val roadNet: EdgeTable
    val roadNodes: NodeTable
    val roadRegions: RegionTable
    val roadAreas: AreaTable
    val areaGeoms: AreaGeometriesTable
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

class DefaultRoadNetTablesWithPrefix(prefix: String) extends RoadNetTables {
    val roadNet = new EdgeTable(prefix + "road_net")
    val roadNodes = new NodeTable(prefix + "road_nodes")
    val roadRegions = new RegionTable(prefix + "road_regions")
    val roadAreas = new AreaTable(prefix + "road_areas")
    val areaGeoms = new AreaGeometriesTable(prefix + "area_geoms")
}

trait ConfigRoadNetTableComponent extends RoadNetTableComponent with RoadNetTableParsingComponent {
    self: ArgumentParser =>
    val roadNetTables = new DefaultRoadNetTablesWithPrefix(roadNetPrefixLens.get(parsedConfig))
}

trait HigherConfigRoadNetTableComponent extends HigherLevelRoadNetTableComponent with HigherRoadNetTableParsingComponent {
    self: ArgumentParser =>

    val higherRoadNetTables = new DefaultRoadNetTablesWithPrefix(higherRoadNetPrefixLens.get(parsedConfig))
}

trait RoadNetTableParsingComponent extends OptionParserComponent {
    lazy val roadNetPrefixLens = registerConfig("")

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[String]('r', "roads").action((x, c) => roadNetPrefixLens.set(c)(x))
    }
}

trait HigherRoadNetTableParsingComponent extends OptionParserComponent {
    lazy val higherRoadNetPrefixLens = registerConfig("higher_")

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[String]('h', "higher").action((x, c) => higherRoadNetPrefixLens.set(c)(x))
    }
}

trait MultiLevelRoadNetTableParsingComponent extends OptionParserComponent {
    lazy val multiLevelRoadNetPrefixLens = registerConfig(List(""))

    def multiLevelRoadNetPrefixStrLens = new Lens[List[String], String] {
        def get(lst: List[String]) = lst.mkString(",")
        def set(l: List[String])(str: String) = str.split(",").toList
    }.compose(multiLevelRoadNetPrefixLens)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[String]("road-levels").action((x, c) => multiLevelRoadNetPrefixStrLens.set(c)(x))
    }
}

trait MultiLevelRoadNetTableComponent {
    val roadNetTableLevels: List[RoadNetTables]
}

trait ConfigMultiLevelRoadNetTableComponent extends MultiLevelRoadNetTableComponent with MultiLevelRoadNetTableParsingComponent {
    self: ArgumentParser =>
    val roadNetTableLevels = multiLevelRoadNetPrefixLens.get(parsedConfig).map(x => new DefaultRoadNetTablesWithPrefix(x))
}